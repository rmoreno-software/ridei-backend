package com.ridei.circuits.domain.model;

import static com.ridei.circuits.domain.model.CatalogFixtures.MONTMELO;
import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.alcarras;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.montmelo;
import static com.ridei.circuits.domain.model.CatalogFixtures.onlyTrack;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static com.ridei.circuits.domain.model.CatalogFixtures.venue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.ridei.circuits.domain.event.LayoutAdded;
import com.ridei.circuits.domain.event.TrackAdded;
import com.ridei.circuits.domain.event.VenueProposed;
import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.MergedCatalogItemException;

class VenueTest {

    private static final Instant LATER = NOW.plusSeconds(60);

    @Nested
    class Proposal {

        @Test
        void entersPendingReviewWithItsFirstTrackAndDefaultLayout() {
            Venue venue = montmelo();

            assertThat(venue.getStatus()).isEqualTo(CatalogStatus.PENDING_REVIEW);
            assertThat(venue.isNew()).isTrue();
            assertThat(venue.activeTracks()).hasSize(1);
            assertThat(onlyTrack(venue).defaultLayout()).get()
                .extracting(layout -> layout.getName().value()).isEqualTo("GP");
        }

        @Test
        void recordsProposalTrackAndLayoutEvents() {
            assertThat(montmelo().pullDomainEvents())
                .hasExactlyElementsOfTypes(VenueProposed.class, TrackAdded.class, LayoutAdded.class);
        }

        @Test
        void pullingEventsEmptiesThem() {
            Venue venue = montmelo();
            venue.pullDomainEvents();

            assertThat(venue.pullDomainEvents()).isEmpty();
        }
    }

    @Nested
    class Alcarras {

        @Test
        void tracksOfOtherDisciplinesAreNewTracksEvenIfVeryClose() {
            Venue venue = alcarras();

            venue.addTrack(track(Discipline.KARTING, "Karting"), LATER);
            venue.addTrack(track(Discipline.MOTOCROSS, "Motocross"), LATER);

            assertThat(venue.activeTracks()).extracting(Track::getDiscipline)
                .containsExactly(Discipline.ROAD_RACING, Discipline.KARTING, Discipline.MOTOCROSS);
        }

        @Test
        void proposingVelocidadAgainIsADuplicate() {
            Venue venue = alcarras();
            Track existing = onlyTrack(venue);

            assertThatThrownBy(() -> venue.addTrack(track(Discipline.ROAD_RACING, "Pista de velocidad"), LATER))
                .isInstanceOfSatisfying(DuplicateTrackException.class,
                    ex -> assertThat(ex.getExisting().getId()).isEqualTo(existing.getId()));
        }

        @Test
        void sameNameInAnotherDisciplineIsNotADuplicate() {
            Venue venue = alcarras();

            Track mixed = venue.addTrack(track(Discipline.MIXED, "Velocidad"), LATER);

            assertThat(mixed.getStatus()).isEqualTo(CatalogStatus.PENDING_REVIEW);
        }
    }

    @Nested
    class Montmelo {

        @Test
        void addingGpWithChicaneCreatesANonDefaultLayout() {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);

            Layout chicane = venue.addLayout(road.getId(), layout("GP con chicane", 4675, 16, Direction.CLOCKWISE), LATER);

            assertThat(chicane.isDefault()).isFalse();
            assertThat(road.activeLayouts()).hasSize(2);
        }

        @Test
        void variantWithSameGeometryIsADuplicateOfTheExistingLayout() {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout gp = road.defaultLayout().orElseThrow();

            assertThatThrownBy(() ->
                venue.addLayout(road.getId(), layout("Variante F1", 4670, 14, Direction.CLOCKWISE), LATER))
                .isInstanceOfSatisfying(DuplicateLayoutException.class,
                    ex -> assertThat(ex.getExisting().getId()).isEqualTo(gp.getId()));
        }
    }

    @Nested
    class DefaultLayout {

        @Test
        void firstLayoutOfATrackWithoutLayoutsBecomesDefault() {
            Venue venue = alcarras();
            Track road = onlyTrack(venue);

            Layout first = venue.addLayout(road.getId(), layout("Largo", 1800, 12, Direction.CLOCKWISE), LATER);

            assertThat(first.isDefault()).isTrue();
        }

        @Test
        void setDefaultLayoutKeepsExactlyOneDefault() {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout nacional = venue.addLayout(road.getId(), layout("Nacional", 2950, 9, Direction.CLOCKWISE), LATER);

            venue.setDefaultLayout(road.getId(), nacional.getId(), LATER);

            assertThat(road.activeLayouts()).filteredOn(Layout::isDefault).containsExactly(nacional);
        }

        @Test
        void mergingTheDefaultLayoutPromotesTheTarget() {
            Venue venue = montmelo();
            Track road = onlyTrack(venue);
            Layout gp = road.defaultLayout().orElseThrow();
            Layout nacional = venue.addLayout(road.getId(), layout("Nacional", 2950, 9, Direction.CLOCKWISE), LATER);

            venue.mergeLayouts(gp.getId(), nacional.getId(), LATER);

            assertThat(gp.getStatus()).isEqualTo(CatalogStatus.MERGED);
            assertThat(gp.getMergedInto()).isEqualTo(nacional.getId());
            assertThat(road.defaultLayout()).contains(nacional);
        }
    }

    @Nested
    class Corrections {

        @Test
        void renamingATrackToADuplicateIsRejected() {
            Venue venue = alcarras();
            Track karting = venue.addTrack(track(Discipline.KARTING, "Karting"), LATER);
            venue.addTrack(track(Discipline.KARTING, "Kartódromo infantil"), LATER);

            assertThatThrownBy(() ->
                venue.correctTrack(karting.getId(), new TrackCorrection(null, TrackName.of("Kartodromo infantil")), LATER))
                .isInstanceOf(DuplicateTrackException.class);
        }

        @Test
        void correctingALayoutCanKeepItsOwnName() {
            Venue venue = montmelo();
            Layout gp = onlyTrack(venue).defaultLayout().orElseThrow();

            venue.correctLayout(gp.getId(), new LayoutCorrection(null, Length.ofMeters(4675), null, null), LATER);

            assertThat(gp.getLength().meters()).isEqualTo(4675);
        }

        @Test
        void everyMutationTouchesUpdatedAt() {
            Venue venue = montmelo();

            venue.verify(LATER);

            assertThat(venue.getUpdatedAt()).isEqualTo(LATER);
            assertThat(venue.getStatus()).isEqualTo(CatalogStatus.VERIFIED);
        }
    }

    @Test
    void mergedVenuesRejectChanges() {
        Venue source = venue("Montmeló", MONTMELO, track(Discipline.KARTING, "Karting"));
        Venue target = montmelo();
        source.mergeInto(target, LATER);

        assertThatThrownBy(() -> source.addTrack(track(Discipline.MOTOCROSS, "MX"), LATER))
            .isInstanceOf(MergedCatalogItemException.class);
    }
}
