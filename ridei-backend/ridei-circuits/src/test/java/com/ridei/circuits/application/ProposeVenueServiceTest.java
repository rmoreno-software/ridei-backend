package com.ridei.circuits.application;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.PROPOSER;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.event.LayoutAdded;
import com.ridei.circuits.domain.event.TrackAdded;
import com.ridei.circuits.domain.event.VenueProposed;
import com.ridei.circuits.domain.exception.PossibleDuplicateVenueException;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.DeduplicationPolicy;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueName;

class ProposeVenueServiceTest {

    private static final ExternalPlaceId PLACE = new ExternalPlaceId("ChIJ-montmelo");
    /** ~1 km al norte de Montmeló. */
    private static final GeoPoint ONE_KM_AWAY = GeoPoint.of(MONTMELO.latitude() + 0.009, MONTMELO.longitude());
    /** ~5 km al norte de Montmeló. */
    private static final GeoPoint FIVE_KM_AWAY = GeoPoint.of(MONTMELO.latitude() + 0.045, MONTMELO.longitude());

    private final InMemoryVenueRepository repository = new InMemoryVenueRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final ProposeVenueService service = new ProposeVenueService(
        repository, events, DeduplicationPolicy.DEFAULT, Clock.fixed(NOW, ZoneOffset.UTC));

    private Venue existing;

    @BeforeEach
    void setUp() {
        existing = repository.given(Venue.propose(VenueName.of("Circuit de Barcelona-Catalunya"), CountryCode.of("ES"),
            MONTMELO, PLACE, PROPOSER, track(Discipline.ROAD_RACING, "Velocidad"), null, NOW));
    }

    @Test
    void createsAPendingVenueWithItsFirstTrackAndDefaultLayout() {
        ProposeVenueResult result = service.propose(command("Circuito de Navarra", GeoPoint.of(42.56, -2.17), null, false));

        assertThat(result.isCreated()).isTrue();
        Venue venue = result.venue();
        assertThat(venue.getStatus()).isEqualTo(CatalogStatus.PENDING_REVIEW);
        assertThat(venue.getCreatedBy()).isEqualTo(PROPOSER);
        assertThat(venue.activeTracks().get(0).defaultLayout()).isPresent();
        assertThat(events.published)
            .hasExactlyElementsOfTypes(VenueProposed.class, TrackAdded.class, LayoutAdded.class);
    }

    @Test
    void sameExternalPlaceIdAlwaysReturnsTheExistingVenueEvenWhenConfirmed() {
        ProposeVenueResult result = service.propose(command("Otro nombre", FIVE_KM_AWAY, PLACE, true));

        assertThat(result.isCreated()).isFalse();
        assertThat(result.venue()).isSameAs(existing);
        assertThat(repository.saves).isEmpty();
        assertThat(events.published).isEmpty();
    }

    @Test
    void sameExternalPlaceIdOfAMergedVenueReturnsTheCanonicalOne() {
        Venue canonical = repository.given(Venue.propose(VenueName.of("Montmeló"), CountryCode.of("ES"), MONTMELO,
            null, PROPOSER, track(Discipline.KARTING, "Karting"), null, NOW));
        existing.mergeInto(canonical, NOW);

        ProposeVenueResult result = service.propose(command("Montmeló", MONTMELO, PLACE, false));

        assertThat(result.venue()).isSameAs(canonical);
    }

    @Test
    void similarNameNearbyIsAPossibleDuplicateWithCandidates() {
        assertThatThrownBy(() -> service.propose(command("circuito barcelona catalunya", ONE_KM_AWAY, null, false)))
            .isInstanceOfSatisfying(PossibleDuplicateVenueException.class, ex -> {
                assertThat(ex.getCandidates()).singleElement().satisfies(candidate -> {
                    assertThat(candidate.id()).isEqualTo(existing.getId());
                    assertThat(candidate.similarity()).isEqualTo(1.0);
                    assertThat(candidate.distance().meters()).isBetween(900.0, 1100.0);
                });
            });
        assertThat(repository.saves).isEmpty();
    }

    @Test
    void confirmedNotDuplicateCreatesItAnyway() {
        ProposeVenueResult result = service.propose(command("circuito barcelona catalunya", ONE_KM_AWAY, null, true));

        assertThat(result.isCreated()).isTrue();
        assertThat(repository.saves).containsExactly(result.venue().getId());
    }

    @Test
    void totallyDifferentNameOneKilometerAwayIsANewVenue() {
        ProposeVenueResult result = service.propose(command("Karting Parets", ONE_KM_AWAY, null, false));

        assertThat(result.isCreated()).isTrue();
    }

    @Test
    void similarNameOutsideTheRadiusIsANewVenue() {
        ProposeVenueResult result = service.propose(command("Barcelona Catalunya", FIVE_KM_AWAY, null, false));

        assertThat(result.isCreated()).isTrue();
    }

    @Test
    void locksTheAreaBeforeChecking() {
        service.propose(command("Karting Parets", ONE_KM_AWAY, null, false));

        assertThat(repository.lockedAreas).containsExactly(ONE_KM_AWAY);
    }

    private static ProposeVenueCommand command(String name, GeoPoint location, ExternalPlaceId place, boolean confirmed) {
        return new ProposeVenueCommand(PROPOSER, VenueName.of(name), CountryCode.of("ES"), location, place,
            track(Discipline.ROAD_RACING, "Velocidad"), layout("GP", 3933, 15, Direction.CLOCKWISE), confirmed);
    }
}
