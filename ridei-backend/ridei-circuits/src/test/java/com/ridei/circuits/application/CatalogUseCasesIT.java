package com.ridei.circuits.application;

import static com.ridei.circuits.domain.model.CatalogFixtures.PROPOSER;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static com.ridei.circuits.domain.model.CatalogFixtures.track;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ridei.circuits.PostgisIntegrationTest;
import com.ridei.circuits.application.port.in.AddLayoutToTrackUseCase;
import com.ridei.circuits.application.port.in.AddTrackToVenueUseCase;
import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.application.port.in.MergeVenuesUseCase;
import com.ridei.circuits.application.port.in.ProposeVenueUseCase;
import com.ridei.circuits.application.port.in.SetDefaultLayoutUseCase;
import com.ridei.circuits.domain.exception.DuplicateLayoutException;
import com.ridei.circuits.domain.exception.DuplicateTrackException;
import com.ridei.circuits.domain.exception.PossibleDuplicateVenueException;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.Direction;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.model.VenueName;

/** Casos de uso reales (transacciones, reintentos, bloqueos) contra PostGIS. */
class CatalogUseCasesIT extends PostgisIntegrationTest {

    private static final VenueId SEED_MONTMELO = VenueId.of("10000000-0000-4000-8000-000000000001");
    private static final TrackId SEED_MONTMELO_ROAD = TrackId.of("20000000-0000-4000-8000-000000000001");
    private static final VenueId SEED_ALCARRAS = VenueId.of("10000000-0000-4000-8000-000000000010");

    @Autowired ProposeVenueUseCase proposeVenue;
    @Autowired AddTrackToVenueUseCase addTrack;
    @Autowired AddLayoutToTrackUseCase addLayout;
    @Autowired MergeVenuesUseCase mergeVenues;
    @Autowired SetDefaultLayoutUseCase setDefaultLayout;
    @Autowired GetVenueDetailUseCase getVenueDetail;

    private static ProposeVenueCommand proposal(String name, GeoPoint location, String placeId, boolean confirmed) {
        return new ProposeVenueCommand(PROPOSER, VenueName.of(name), CountryCode.of("PT"), location,
            ExternalPlaceId.ofNullable(placeId), track(Discipline.ROAD_RACING, "Velocidad"),
            layout("Largo", 3200, 12, Direction.CLOCKWISE), confirmed);
    }

    // ── Escenarios del catálogo sobre el seed ───────────────────────────────────────────

    @Test
    void alcarrasMotocrossIsANewTrackButVelocidadAgainIsA409() {
        Track motocross = addTrack.addTrack(new AddTrackToVenueCommand(SEED_ALCARRAS, track(Discipline.MOTOCROSS, "Motocross")));

        assertThat(motocross.getDiscipline()).isEqualTo(Discipline.MOTOCROSS);
        assertThatThrownBy(() ->
            addTrack.addTrack(new AddTrackToVenueCommand(SEED_ALCARRAS, track(Discipline.ROAD_RACING, "Velocidad"))))
            .isInstanceOf(DuplicateTrackException.class);
    }

    @Test
    void montmeloVarianteF1WithTheGeometryOfTheGpIsADuplicateOfTheGp() {
        assertThatThrownBy(() -> addLayout.addLayout(new AddLayoutToTrackCommand(SEED_MONTMELO, SEED_MONTMELO_ROAD,
            layout("Variante F1", 4670, 14, Direction.CLOCKWISE))))
            .isInstanceOfSatisfying(DuplicateLayoutException.class,
                ex -> assertThat(ex.getExisting().getName().value()).isEqualTo("GP"));
    }

    @Test
    void barcelonaTypedDifferentlyNextToMontmeloIsAPossibleDuplicate() {
        assertThatThrownBy(() -> proposeVenue.propose(new ProposeVenueCommand(PROPOSER,
            VenueName.of("circuito barcelona catalunya"), CountryCode.of("ES"), GeoPoint.of(41.575, 2.2611), null,
            track(Discipline.ROAD_RACING, "Velocidad"), null, false)))
            .isInstanceOfSatisfying(PossibleDuplicateVenueException.class,
                ex -> assertThat(ex.getCandidates()).extracting(c -> c.id()).containsExactly(SEED_MONTMELO));
    }

    // ── Merge y default layout persistidos ──────────────────────────────────────────────

    @Test
    void mergingVenuesWithConflictingTracksPersistsTheFusionAndTheCanonicalRedirect() {
        GeoPoint here = somewhereInTheOcean();
        Venue target = proposeVenue.propose(proposal("Autódromo Atlántico", here, null, false)).venue();
        Venue source = proposeVenue.propose(proposal("Autódromo Atlántico", here, null, true)).venue();
        addTrack.addTrack(new AddTrackToVenueCommand(source.getId(), track(Discipline.KARTING, "Karting")));

        mergeVenues.merge(new MergeVenuesCommand(source.getId(), target.getId()));

        VenueDetail detail = getVenueDetail.get(source.getId());
        assertThat(detail.redirectedFromMerged()).isTrue();
        Venue canonical = detail.venue();
        assertThat(canonical.getId()).isEqualTo(target.getId());
        assertThat(canonical.activeTracks()).extracting(Track::getDiscipline)
            .containsExactly(Discipline.ROAD_RACING, Discipline.KARTING);
        assertThat(canonical.getTracks()).filteredOn(t -> t.getStatus() == CatalogStatus.MERGED).hasSize(1);
        assertThat(canonical.activeTracks().get(0).activeLayouts()).hasSize(1);
    }

    @Test
    void changingTheDefaultLayoutSwapsTheFlagWithinOneTransaction() {
        Venue venue = proposeVenue.propose(proposal("Autódromo Defaults", somewhereInTheOcean(), null, false)).venue();
        TrackId road = venue.activeTracks().get(0).getId();
        Layout corto = addLayout.addLayout(new AddLayoutToTrackCommand(venue.getId(), road,
            layout("Corto", 1800, 7, Direction.CLOCKWISE)));

        Track updated = setDefaultLayout.setDefault(new SetDefaultLayoutCommand(venue.getId(), road, corto.getId()));

        assertThat(updated.defaultLayout()).get().extracting(Layout::getId).isEqualTo(corto.getId());
        assertThat(updated.activeLayouts()).filteredOn(Layout::isDefault).hasSize(1);
    }

    // ── Condiciones de carrera: una gana, la otra recibe el existente o un 409; nunca un 500 ─

    @Test
    void concurrentProposalsWithTheSameExternalPlaceIdCreateOneVenue() throws Exception {
        String placeId = "place-" + UUID.randomUUID();
        GeoPoint here = somewhereInTheOcean();

        List<Object> outcomes = race(() -> proposeVenue.propose(proposal("Autódromo Carrera", here, placeId, true)));

        assertThat(outcomes).allMatch(ProposeVenueResult.class::isInstance);
        List<ProposeVenueResult> results = outcomes.stream().map(ProposeVenueResult.class::cast).toList();
        assertThat(results).filteredOn(ProposeVenueResult::isCreated).hasSize(1);
        assertThat(results).extracting(r -> r.venue().getId()).containsOnly(results.get(0).venue().getId());
    }

    /**
     * Mismo place id pero coordenadas a 50 km: los bloqueos de zona no coinciden, así que la
     * barrera es el UNIQUE de external_place_id. El perdedor reintenta y recibe el existente.
     */
    @Test
    void samePlaceIdFarApartIsResolvedByTheUniqueIndexAndTheRetry() throws Exception {
        String placeId = "place-" + UUID.randomUUID();
        GeoPoint here = somewhereInTheOcean();
        GeoPoint farAway = northOf(here, 50_000);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Object> first = pool.submit(() -> attempt(start, proposal("Autódromo Norte", here, placeId, true)));
            Future<Object> second = pool.submit(() -> attempt(start, proposal("Autódromo Sur", farAway, placeId, true)));
            start.countDown();
            List<Object> outcomes = List.of(first.get(), second.get());

            assertThat(outcomes).allMatch(ProposeVenueResult.class::isInstance);
            List<ProposeVenueResult> results = outcomes.stream().map(ProposeVenueResult.class::cast).toList();
            assertThat(results).filteredOn(ProposeVenueResult::isCreated).hasSize(1);
            assertThat(results).extracting(r -> r.venue().getId()).containsOnly(results.get(0).venue().getId());
        } finally {
            pool.shutdownNow();
        }
    }

    private Object attempt(CountDownLatch start, ProposeVenueCommand command) throws InterruptedException {
        start.await();
        try {
            return proposeVenue.propose(command);
        } catch (RuntimeException ex) {
            return ex;
        }
    }

    @Test
    void concurrentProposalsOfTheSameVenueWithoutPlaceIdYieldOneVenueAndOneConflict() throws Exception {
        GeoPoint here = somewhereInTheOcean();

        List<Object> outcomes = race(() -> proposeVenue.propose(proposal("Autódromo Simultáneo", here, null, false)));

        assertThat(outcomes).filteredOn(ProposeVenueResult.class::isInstance).hasSize(1);
        assertThat(outcomes).filteredOn(PossibleDuplicateVenueException.class::isInstance).hasSize(1);
    }

    @Test
    void concurrentAdditionsOfTheSameTrackYieldOneTrackAndOneConflict() throws Exception {
        Venue venue = proposeVenue.propose(proposal("Autódromo Pistas", somewhereInTheOcean(), null, false)).venue();

        List<Object> outcomes = race(() ->
            addTrack.addTrack(new AddTrackToVenueCommand(venue.getId(), track(Discipline.KARTING, "Karting"))));

        assertThat(outcomes).filteredOn(Track.class::isInstance).hasSize(1);
        assertThat(outcomes).filteredOn(DuplicateTrackException.class::isInstance).hasSize(1);
        assertThat(getVenueDetail.get(venue.getId()).venue().activeTracks()).hasSize(2);
    }

    /** Lanza la misma operación en dos hilos a la vez; devuelve el resultado o la excepción de cada uno. */
    private static List<Object> race(Callable<Object> operation) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            List<Future<Object>> futures = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                futures.add(pool.submit(() -> {
                    start.await();
                    try {
                        return operation.call();
                    } catch (RuntimeException ex) {
                        return ex;
                    }
                }));
            }
            start.countDown();
            List<Object> outcomes = new ArrayList<>();
            for (Future<Object> future : futures) outcomes.add(future.get());
            return outcomes;
        } finally {
            pool.shutdownNow();
        }
    }
}
