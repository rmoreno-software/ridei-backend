package com.ridei.circuits.application;

import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.alcarras;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.event.LayoutAdded;
import com.ridei.circuits.domain.event.TrackAdded;
import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.VenueNotFoundException;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

class AddTrackAndLayoutServiceTest {

    private final InMemoryVenueRepository repository = new InMemoryVenueRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final Clock clock = Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC);
    private final AddTrackToVenueService addTrack = new AddTrackToVenueService(repository, events, clock);
    private final AddLayoutToTrackService addLayout = new AddLayoutToTrackService(repository, events, clock);

    @Test
    void alcarrasKartingAndMotocrossAreNewTracksButVelocidadAgainIsADuplicate() {
        Venue venue = repository.given(alcarras());

        Track karting = addTrack.addTrack(new AddTrackToVenueCommand(venue.getId(), track(Discipline.KARTING, "Karting")));
        Track motocross = addTrack.addTrack(new AddTrackToVenueCommand(venue.getId(), track(Discipline.MOTOCROSS, "Motocross")));

        assertThat(venue.activeTracks()).extracting(Track::getId)
            .contains(karting.getId(), motocross.getId());
        assertThat(events.published).hasOnlyElementsOfType(TrackAdded.class).hasSize(2);
        assertThatThrownBy(() ->
            addTrack.addTrack(new AddTrackToVenueCommand(venue.getId(), track(Discipline.ROAD_RACING, "Velocidad"))))
            .isInstanceOf(DuplicateTrackException.class);
    }

    @Test
    void montmeloGpWithChicaneIsNewButVarianteF1WithSameGeometryIsADuplicate() {
        Venue venue = repository.given(montmelo());
        Track road = onlyTrack(venue);
        Layout gp = road.defaultLayout().orElseThrow();

        Layout chicane = addLayout.addLayout(new AddLayoutToTrackCommand(venue.getId(), road.getId(),
            layout("GP con chicane", 4675, 16, Direction.CLOCKWISE)));

        assertThat(chicane.isDefault()).isFalse();
        assertThat(events.published).singleElement().isInstanceOf(LayoutAdded.class);
        assertThatThrownBy(() -> addLayout.addLayout(new AddLayoutToTrackCommand(venue.getId(), road.getId(),
            layout("Variante F1", 4640, 14, Direction.CLOCKWISE))))
            .isInstanceOfSatisfying(DuplicateLayoutException.class,
                ex -> assertThat(ex.getExisting().getId()).isEqualTo(gp.getId()));
    }

    @Test
    void unknownVenueIsNotFound() {
        assertThatThrownBy(() ->
            addTrack.addTrack(new AddTrackToVenueCommand(VenueId.newId(), track(Discipline.KARTING, "Karting"))))
            .isInstanceOf(VenueNotFoundException.class);
    }
}
