package com.ridei.circuits.domain.model;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static com.ridei.circuits.domain.model.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.event.DomainEvent;
import com.ridei.circuits.domain.event.LayoutMerged;
import com.ridei.circuits.domain.event.TrackMerged;
import com.ridei.circuits.domain.event.VenueMerged;
import com.ridei.circuits.domain.exception.InvalidMergeException;

class VenueMergeTest {

    private static final Instant LATER = NOW.plusSeconds(60);

    private Venue target;
    private Venue source;
    private Track targetRoad;
    private Track sourceRoad;
    private Track sourceKarting;
    private Layout targetGp;

    @BeforeEach
    void setUp() {
        target = montmelo();
        targetRoad = onlyTrack(target);
        targetGp = targetRoad.defaultLayout().orElseThrow();

        source = venue("Circuito Montmeló", MONTMELO, track(Discipline.ROAD_RACING, "Pista de velocidad"),
            layout("Gran Premio F1", 4660, 14, Direction.CLOCKWISE));
        sourceRoad = onlyTrack(source);
        source.addLayout(sourceRoad.getId(), layout("Escuela", 1650, 6, Direction.CLOCKWISE), NOW);
        sourceKarting = source.addTrack(track(Discipline.KARTING, "Karting"), NOW);

        target.pullDomainEvents();
        source.pullDomainEvents();
    }

    @Test
    void movesEveryTrackKeepingItsId() {
        source.mergeInto(target, LATER);

        assertThat(source.getStatus()).isEqualTo(CatalogStatus.MERGED);
        assertThat(source.getMergedInto()).isEqualTo(target.getId());
        assertThat(source.getTracks()).isEmpty();
        assertThat(target.getTracks()).extracting(Track::getId)
            .containsExactly(targetRoad.getId(), sourceRoad.getId(), sourceKarting.getId());
    }

    @Test
    void conflictingTracksAreMergedTogetherWithTheirLayouts() {
        Layout sourceGp = sourceRoad.getLayouts().get(0);
        Layout escuela = sourceRoad.getLayouts().get(1);

        source.mergeInto(target, LATER);

        assertThat(sourceRoad.getStatus()).isEqualTo(CatalogStatus.MERGED);
        assertThat(sourceRoad.getMergedInto()).isEqualTo(targetRoad.getId());
        assertThat(sourceGp.getMergedInto()).isEqualTo(targetGp.getId());
        assertThat(targetRoad.activeLayouts()).containsExactly(targetGp, escuela);
        assertThat(targetRoad.defaultLayout()).contains(targetGp);
        assertThat(target.activeTracks()).extracting(Track::getDiscipline)
            .containsExactly(Discipline.ROAD_RACING, Discipline.KARTING);
    }

    @Test
    void publishesVenueTrackAndLayoutMergeEvents() {
        Layout sourceGp = sourceRoad.getLayouts().get(0);
        Layout escuela = sourceRoad.getLayouts().get(1);

        source.mergeInto(target, LATER);
        List<DomainEvent> events = source.pullDomainEvents();

        assertThat(events).containsExactly(
            new TrackMerged(sourceRoad.getId(), targetRoad.getId(), target.getId(), List.of(escuela.getId()), LATER),
            new LayoutMerged(sourceGp.getId(), targetGp.getId(), targetRoad.getId(), LATER),
            new VenueMerged(source.getId(), target.getId(), List.of(sourceRoad.getId(), sourceKarting.getId()), LATER)
        );
    }

    @Test
    void touchesBothAggregates() {
        source.mergeInto(target, LATER);

        assertThat(source.getUpdatedAt()).isEqualTo(LATER);
        assertThat(target.getUpdatedAt()).isEqualTo(LATER);
    }

    @Test
    void cannotMergeIntoItself() {
        assertThatThrownBy(() -> target.mergeInto(target, LATER)).isInstanceOf(InvalidMergeException.class);
    }

    @Test
    void tracksOfDifferentDisciplinesCannotBeMerged() {
        assertThatThrownBy(() -> source.mergeTrackInto(sourceKarting.getId(), target, targetRoad.getId(), LATER))
            .isInstanceOf(InvalidMergeException.class);
    }

    @Test
    void mergeTrackAcrossVenuesLeavesTheSourceTrackMergedInItsVenue() {
        source.mergeTrackInto(sourceRoad.getId(), target, targetRoad.getId(), LATER);

        assertThat(source.getStatus()).isEqualTo(CatalogStatus.PENDING_REVIEW);
        assertThat(source.track(sourceRoad.getId()).getStatus()).isEqualTo(CatalogStatus.MERGED);
        assertThat(targetRoad.activeLayouts()).hasSize(2);
    }

    @Test
    void layoutsOfDifferentTracksCannotBeMergedDirectly() {
        Track karting = target.addTrack(track(Discipline.KARTING, "Karting"), LATER);
        Layout kart = target.addLayout(karting.getId(), layout("Largo", 1200, 10, Direction.CLOCKWISE), LATER);

        assertThatThrownBy(() -> target.mergeLayouts(kart.getId(), targetGp.getId(), LATER))
            .isInstanceOf(InvalidMergeException.class);
    }
}
