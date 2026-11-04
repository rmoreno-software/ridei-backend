package com.ridei.circuits.application;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static com.ridei.circuits.domain.model.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.event.LayoutMerged;
import com.ridei.circuits.domain.event.TrackMerged;
import com.ridei.circuits.domain.event.VenueMerged;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;

class MergeServicesTest {

    private final InMemoryVenueRepository repository = new InMemoryVenueRepository();
    private final RecordingEventPublisher events = new RecordingEventPublisher();
    private final Clock clock = Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC);

    private Venue target;
    private Venue source;

    @BeforeEach
    void setUp() {
        target = repository.given(montmelo());
        Venue duplicate = venue("Montmeló", MONTMELO, track(Discipline.ROAD_RACING, "Velocidad"),
            layout("Gran Premio", 4650, 14, Direction.CLOCKWISE));
        duplicate.addTrack(track(Discipline.KARTING, "Karting"), NOW);
        source = repository.given(duplicate);
    }

    @Test
    void mergeVenuesFusesConflictingTracksSavesSourceFirstAndPublishesEvents() {
        Venue merged = new MergeVenuesService(repository, events, clock)
            .merge(new MergeVenuesCommand(source.getId(), target.getId()));

        assertThat(repository.saves).containsExactly(source.getId(), target.getId());
        assertThat(source.getStatus()).isEqualTo(CatalogStatus.MERGED);
        assertThat(merged.activeTracks()).extracting(Track::getDiscipline)
            .containsExactly(Discipline.ROAD_RACING, Discipline.KARTING);
        assertThat(onlyTrack(merged).activeLayouts()).hasSize(1);
        assertThat(events.published)
            .hasExactlyElementsOfTypes(TrackMerged.class, LayoutMerged.class, VenueMerged.class);
    }

    @Test
    void mergeTracksAcrossVenuesSavesBothAggregates() {
        Track sourceRoad = source.activeTracks().get(0);
        Track targetRoad = onlyTrack(target);

        new MergeTracksService(repository, events, clock)
            .merge(new MergeTracksCommand(sourceRoad.getId(), targetRoad.getId()));

        assertThat(repository.saves).containsExactly(source.getId(), target.getId());
        assertThat(sourceRoad.getMergedInto()).isEqualTo(targetRoad.getId());
        assertThat(events.published).hasExactlyElementsOfTypes(TrackMerged.class, LayoutMerged.class);
    }

    @Test
    void mergeTracksWithinOneVenueSavesItOnce() {
        Track karting = source.activeTracks().get(1);
        Track otherKarting = source.addTrack(track(Discipline.KARTING, "Kartódromo infantil"), NOW);
        source.pullDomainEvents();

        new MergeTracksService(repository, events, clock)
            .merge(new MergeTracksCommand(otherKarting.getId(), karting.getId()));

        assertThat(repository.saves).containsExactly(source.getId());
        assertThat(otherKarting.getStatus()).isEqualTo(CatalogStatus.MERGED);
    }
}
