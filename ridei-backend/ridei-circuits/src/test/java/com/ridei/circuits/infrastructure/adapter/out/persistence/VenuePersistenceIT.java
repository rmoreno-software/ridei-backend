package com.ridei.circuits.infrastructure.adapter.out.persistence;

import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.PROPOSER;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ridei.circuits.PostgisIntegrationTest;
import com.ridei.circuits.domain.exception.CatalogItemAlreadyExistsException;
import com.ridei.circuits.domain.exception.ConcurrentCatalogModificationException;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.PageRequest;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.SimilarityThreshold;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueCandidate;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.model.VenueName;
import com.ridei.circuits.domain.model.VenueSearchCriteria;
import com.ridei.circuits.domain.model.VenueSearchHit;
import com.ridei.circuits.domain.port.out.VenueRepositoryPort;

class VenuePersistenceIT extends PostgisIntegrationTest {

    private static final GeoPoint MONTMELO = GeoPoint.of(41.57000, 2.26111);
    private static final VenueId SEED_MONTMELO = VenueId.of("10000000-0000-4000-8000-000000000001");
    private static final VenueId SEED_JEREZ = VenueId.of("10000000-0000-4000-8000-000000000002");
    private static final VenueId SEED_CASTELLOLI = VenueId.of("10000000-0000-4000-8000-000000000011");

    @Autowired VenueRepositoryPort repository;
    @Autowired JdbcTemplate jdbc;

    private Venue newVenue(String name, GeoPoint location, String placeId) {
        return Venue.propose(VenueName.of(name), CountryCode.of("PT"), location, ExternalPlaceId.ofNullable(placeId),
            PROPOSER, track(Discipline.ROAD_RACING, "Velocidad"), layout("Largo", 3000, null, Direction.CLOCKWISE), NOW);
    }

    @Nested
    class Mapping {

        @Test
        void roundTripsTheAggregateIncludingNullableFields() {
            Venue saved = repository.save(newVenue("Autódromo del Océano", somewhereInTheOcean(), null));

            Venue loaded = repository.findById(saved.getId()).orElseThrow();

            assertThat(loaded.getVersion()).isZero();
            assertThat(loaded.getExternalPlaceId()).isNull();
            assertThat(loaded.getMergedInto()).isNull();
            assertThat(loaded.getCreatedBy()).isEqualTo(PROPOSER);
            Layout largo = loaded.activeTracks().get(0).defaultLayout().orElseThrow();
            assertThat(largo.getTurns()).isNull();
            assertThat(largo.getLength().meters()).isEqualTo(3000);
        }

        @Test
        void eachSaveIncrementsTheOptimisticVersion() {
            Venue saved = repository.save(newVenue("Autódromo Versionado", somewhereInTheOcean(), null));
            saved.addTrack(track(Discipline.KARTING, "Karting"), NOW.plusSeconds(1));

            assertThat(repository.save(saved).getVersion()).isEqualTo(1);
        }

        @Test
        void findsTheOwnerOfATrackAndOfALayout() {
            Venue saved = repository.save(newVenue("Autódromo Propietario", somewhereInTheOcean(), null));
            Track road = saved.activeTracks().get(0);
            Layout largo = road.defaultLayout().orElseThrow();

            assertThat(repository.findByTrackId(road.getId())).get().extracting(Venue::getId).isEqualTo(saved.getId());
            assertThat(repository.findByLayoutId(largo.getId())).get().extracting(Venue::getId).isEqualTo(saved.getId());
        }
    }

    @Nested
    class NativeQueries {

        @Test
        void barcelonaSpellingsOneKilometerAwayAreAPotentialDuplicateOfTheSeed() {
            List<VenueCandidate> candidates = repository.findPotentialDuplicates(
                VenueName.of("circuito barcelona catalunya"), northOf(MONTMELO, 1000),
                Distance.ofKilometers(2), SimilarityThreshold.DEFAULT);

            assertThat(candidates).singleElement().satisfies(candidate -> {
                assertThat(candidate.id()).isEqualTo(SEED_MONTMELO);
                assertThat(candidate.similarity()).isEqualTo(1.0);
                assertThat(candidate.distance().meters()).isBetween(950.0, 1050.0);
                assertThat(candidate.status()).isEqualTo(CatalogStatus.VERIFIED);
            });
        }

        @Test
        void totallyDifferentNameOneKilometerAwayIsNotADuplicate() {
            assertThat(repository.findPotentialDuplicates(VenueName.of("Karting Parets del Vallès"),
                northOf(MONTMELO, 1000), Distance.ofKilometers(2), SimilarityThreshold.DEFAULT)).isEmpty();
        }

        @Test
        void similarNameBeyondTheRadiusIsNotADuplicate() {
            assertThat(repository.findPotentialDuplicates(VenueName.of("Barcelona Catalunya"),
                northOf(MONTMELO, 5000), Distance.ofKilometers(2), SimilarityThreshold.DEFAULT)).isEmpty();
        }

        @Test
        void freeTextSearchMatchesWordsOfVenueNames() {
            PageResult<VenueSearchHit> page = repository.search(
                VenueSearchCriteria.of("Jerez", null, null, null, null), PageRequest.of(0, 10));

            assertThat(page.content()).first().satisfies(hit -> {
                assertThat(hit.venue().getId()).isEqualTo(SEED_JEREZ);
                assertThat(hit.similarity()).isEqualTo(1.0);
            });
        }

        @Test
        void searchFiltersByDisciplineAndCountry() {
            PageResult<VenueSearchHit> motocross = repository.search(
                VenueSearchCriteria.of(null, CountryCode.of("ES"), Discipline.MOTOCROSS, null, null), PageRequest.of(0, 50));

            assertThat(motocross.content()).extracting(hit -> hit.venue().getId()).contains(SEED_CASTELLOLI)
                .doesNotContain(SEED_MONTMELO, SEED_JEREZ);
        }

        @Test
        void searchByProximityReturnsDistances() {
            PageResult<VenueSearchHit> page = repository.search(
                VenueSearchCriteria.of(null, null, null, MONTMELO, Distance.ofKilometers(1)), PageRequest.of(0, 10));

            assertThat(page.content()).singleElement().satisfies(hit -> {
                assertThat(hit.venue().getId()).isEqualTo(SEED_MONTMELO);
                assertThat(hit.distance().meters()).isLessThan(1.0);
            });
        }

        @Test
        void nearbyIsOrderedByDistanceAndPaginated() {
            PageResult<VenueSearchHit> first = repository.findNearby(MONTMELO, Distance.ofKilometers(150), PageRequest.of(0, 1));
            PageResult<VenueSearchHit> second = repository.findNearby(MONTMELO, Distance.ofKilometers(150), PageRequest.of(1, 1));

            assertThat(first.totalElements()).isGreaterThanOrEqualTo(3); // Montmeló, Castellolí, Calafat
            assertThat(first.content().get(0).venue().getId()).isEqualTo(SEED_MONTMELO);
            assertThat(second.content().get(0).venue().getId()).isEqualTo(SEED_CASTELLOLI);
            assertThat(second.content().get(0).distance()).isGreaterThan(first.content().get(0).distance());
        }

        @Test
        void mergedVenuesAreExcludedFromSearches() {
            GeoPoint here = somewhereInTheOcean();
            Venue target = repository.save(newVenue("Autódromo Canónico", here, null));
            Venue source = repository.save(newVenue("Autódromo Duplicado", northOf(here, 300), null));
            Venue loadedSource = repository.findById(source.getId()).orElseThrow();
            Venue loadedTarget = repository.findById(target.getId()).orElseThrow();
            loadedSource.mergeInto(loadedTarget, NOW.plusSeconds(1));
            repository.save(loadedSource);
            repository.save(loadedTarget);

            assertThat(repository.findNearby(here, Distance.ofKilometers(1), PageRequest.of(0, 10)).content())
                .extracting(hit -> hit.venue().getId()).containsExactly(target.getId());
        }
    }

    @Nested
    class Constraints {

        @Test
        void sameExternalPlaceIdIsRejectedByTheUniqueIndex() {
            String placeId = "place-" + UUID.randomUUID();
            repository.save(newVenue("Autódromo Uno", somewhereInTheOcean(), placeId));

            assertThatThrownBy(() -> repository.save(newVenue("Autódromo Dos", somewhereInTheOcean(), placeId)))
                .isInstanceOf(CatalogItemAlreadyExistsException.class);
        }

        @Test
        void staleCopyOfTheAggregateIsRejectedByTheOptimisticLock() {
            Venue saved = repository.save(newVenue("Autódromo Concurrente", somewhereInTheOcean(), null));
            Venue first = repository.findById(saved.getId()).orElseThrow();
            Venue second = repository.findById(saved.getId()).orElseThrow();
            first.addTrack(track(Discipline.KARTING, "Karting"), NOW.plusSeconds(1));
            second.addTrack(track(Discipline.KARTING, "Karting"), NOW.plusSeconds(2));

            repository.save(first);

            assertThatThrownBy(() -> repository.save(second)).isInstanceOf(ConcurrentCatalogModificationException.class);
        }

        @Test
        void duplicateActiveTrackIsRejectedByTheDatabase() {
            Venue saved = repository.save(newVenue("Autódromo Restricciones", somewhereInTheOcean(), null));

            assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO track (id, venue_id, discipline, name, normalized_name, status, created_at)
                VALUES (?, ?, 'ROAD_RACING', 'Pista de velocidad', 'velocidad', 'PENDING_REVIEW', now())
                """, UUID.randomUUID(), saved.getId().value()))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void duplicateActiveLayoutNameIsRejectedByTheDatabase() {
            Venue saved = repository.save(newVenue("Autódromo Trazados", somewhereInTheOcean(), null));
            UUID trackId = saved.activeTracks().get(0).getId().value();

            assertThatThrownBy(() -> insertLayout(trackId, "largo", false))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        @Test
        void secondDefaultLayoutIsRejectedByTheDatabase() {
            Venue saved = repository.save(newVenue("Autódromo Defaults", somewhereInTheOcean(), null));
            UUID trackId = saved.activeTracks().get(0).getId().value();

            assertThatThrownBy(() -> insertLayout(trackId, "corto", true))
                .isInstanceOf(DataIntegrityViolationException.class);
        }

        private void insertLayout(UUID trackId, String normalizedName, boolean isDefault) {
            jdbc.update("""
                INSERT INTO layout (id, track_id, name, normalized_name, length_m, turns, direction, is_default, status, created_at)
                VALUES (?, ?, ?, ?, 1500, 8, 'CLOCKWISE', ?, 'PENDING_REVIEW', now())
                """, UUID.randomUUID(), trackId, normalizedName, normalizedName, isDefault);
        }
    }
}
