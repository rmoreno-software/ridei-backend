package com.ridei.sessions.application;

import static com.ridei.sessions.domain.CalafatFixture.LAYOUT;
import static com.ridei.sessions.domain.CalafatFixture.R6;
import static com.ridei.sessions.domain.CalafatFixture.ride;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.sessions.application.SessionUseCasesTest.InMemorySessions;
import com.ridei.sessions.application.SessionUseCasesTest.InMemoryTelemetry;
import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.exception.ManualLapTimeIdConflictException;
import com.ridei.sessions.domain.exception.ManualLapTimeNotFoundException;
import com.ridei.sessions.domain.exception.MotorbikeNotFoundException;
import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.CatalogVenue.Layout;
import com.ridei.sessions.domain.model.CatalogVenue.Track;
import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.ManualLapTimeId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.PersonalRecord.ManualPoint;
import com.ridei.sessions.domain.model.PersonalRecord.SessionBest;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.TimeSource;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.ManualLapTimeRepositoryPort;
import com.ridei.sessions.domain.port.out.VenueCatalogPort;
import com.ridei.sessions.domain.service.CatalogMatcher;
import com.ridei.sessions.domain.service.ManualTimeNotesParserTest;

class ManualLapTimeUseCasesTest {

    static final RiderId ROGER = SessionUseCasesTest.ROGER;
    static final RiderId OTHER = SessionUseCasesTest.OTHER;
    static final DoubleUnaryOperator PROFILE = SessionUseCasesTest.PROFILE;

    /** Una moto que ya no tiene: el garaje la sigue devolviendo. */
    static final MotorbikeSnapshot CBR = new MotorbikeSnapshot(
        MotorbikeId.of("40000000-0000-4000-8000-000000000002"), "Honda CBR600", "SPORT", "ASPHALT");

    /** Calafat tal cual el catálogo: Principal (con meta, el de LAYOUT) y Corto (sin meta ni dibujo). */
    static final LayoutId CORTO = LayoutId.of("30000000-0000-4000-8000-000000000007");
    static final CatalogVenue CALAFAT = new CatalogVenue(LAYOUT.venueId(), "Circuit de Calafat", List.of(
        new Track("Velocidad", "ROAD_RACING", List.of(new Layout(LAYOUT.layoutId(), "Principal", true), new Layout(CORTO, "Corto", false)))));
    static final CatalogVenue MONTMELO = new CatalogVenue(VenueId.of(UUID.randomUUID()), "Circuit de Barcelona-Catalunya (Montmeló)", List.of(
        new Track("Velocidad", "ROAD_RACING", List.of(new Layout(LayoutId.of(UUID.randomUUID()), "GP", true),
            new Layout(LayoutId.of(UUID.randomUUID()), "Nacional", false)))));
    static final CatalogVenue CASTELLOLI = new CatalogVenue(VenueId.of(UUID.randomUUID()), "Parcmotor Castellolí", List.of(
        new Track("Velocidad", "ROAD_RACING", List.of(new Layout(LayoutId.of(UUID.randomUUID()), "Principal", true)))));
    static final CatalogVenue ALCARRAS = new CatalogVenue(VenueId.of(UUID.randomUUID()), "Circuit d'Alcarràs", List.of(
        new Track("Velocidad", "ROAD_RACING", List.of(new Layout(LayoutId.of(UUID.randomUUID()), "Principal", true))),
        new Track("Karting", "KARTING", List.of(new Layout(LayoutId.of(UUID.randomUUID()), "Principal", true))),
        new Track("Motocross", "MOTOCROSS", List.of(new Layout(LayoutId.of(UUID.randomUUID()), "MX", true)))));

    InMemoryManualLapTimes manualTimes;
    InMemorySessions sessions;
    FakeCatalog catalog;
    RecordManualLapTimesService record;
    ManualLapTimesService manage;
    PreviewManualTimesImportService preview;
    PersonalRecordsService records;
    ListSessionsService listSessions;
    RecordSessionService recordSession;

    @BeforeEach
    void setUp() {
        manualTimes = new InMemoryManualLapTimes();
        sessions = new InMemorySessions();
        catalog = new FakeCatalog(List.of(CALAFAT, MONTMELO, CASTELLOLI, ALCARRAS));
        Clock clock = Clock.fixed(Instant.parse("2026-11-28T18:00:00Z"), ZoneOffset.UTC);
        GaragePort garage = id -> id.equals(R6.motorbikeId()) ? Optional.of(R6) : id.equals(CBR.motorbikeId()) ? Optional.of(CBR) : Optional.empty();
        LayoutCatalogPort layouts = (venue, layout) -> layout.equals(LAYOUT.layoutId()) ? Optional.of(LAYOUT) : Optional.empty();
        record = new RecordManualLapTimesService(manualTimes, catalog, garage, clock);
        manage = new ManualLapTimesService(manualTimes);
        preview = new PreviewManualTimesImportService(catalog, clock);
        records = new PersonalRecordsService(sessions, manualTimes);
        listSessions = new ListSessionsService(sessions);
        recordSession = new RecordSessionService(sessions, new InMemoryTelemetry(), layouts, garage, clock);
    }

    @Test
    void recordsATimeOnALayoutWithoutFinishLineWithAFormerMotorbike() {
        RecordManualLapTimeResult result = record.record(command(ManualLapTimeId.newId(), ROGER, CORTO, CBR, LocalDate.of(2023, 3, 3), 109_800));

        assertThat(result.created()).isTrue();
        ManualLapTime time = result.time();
        assertThat(time.layout().layoutName()).isEqualTo("Corto");
        assertThat(time.layout().trackName()).isEqualTo("Velocidad");
        assertThat(time.motorbike().displayName()).isEqualTo("Honda CBR600");
        assertThat(time.entry().precision()).isEqualTo(LapTimePrecision.TENTHS);
    }

    @Test
    void retryingReturnsTheSameTimeAndAnotherRiderCannotReuseTheId() {
        ManualLapTimeId id = ManualLapTimeId.newId();
        record.record(command(id, ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 105_300));

        RecordManualLapTimeResult retry = record.record(command(id, ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 199_999));

        assertThat(retry.created()).isFalse();
        assertThat(retry.time().bestLapMillis()).isEqualTo(105_300);
        assertThat(manualTimes.store).hasSize(1);
        assertThatThrownBy(() -> record.record(command(id, OTHER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 105_300)))
            .isInstanceOf(ManualLapTimeIdConflictException.class);
    }

    @Test
    void rejectsUnknownLayoutsForeignMotorbikesAndFutureDates() {
        LocalDate date = LocalDate.of(2024, 5, 12);
        MotorbikeSnapshot foreign = new MotorbikeSnapshot(MotorbikeId.of("99999999-9999-4999-8999-999999999999"), "Ajena", null, null);

        assertThatThrownBy(() -> record.record(command(ManualLapTimeId.newId(), ROGER, LayoutId.of(UUID.randomUUID()), R6, date, 105_300)))
            .isInstanceOf(LayoutNotFoundException.class);
        assertThatThrownBy(() -> record.record(command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), foreign, date, 105_300)))
            .isInstanceOf(MotorbikeNotFoundException.class);
        assertThatThrownBy(() -> record.record(command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2026, 11, 30), 105_300)))
            .hasMessageContaining("future");
        assertThat(manualTimes.store).isEmpty();
    }

    @Test
    void importsSeveralTimesLookingUpEachCircuitOnce() {
        List<RecordManualLapTimeCommand> commands = List.of(
            command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 105_300),
            command(ManualLapTimeId.newId(), ROGER, CORTO, R6, LocalDate.of(2024, 6, 12), 80_000));

        List<RecordManualLapTimeResult> results = record.recordAll(commands);

        assertThat(results).allMatch(RecordManualLapTimeResult::created);
        assertThat(catalog.findVenueCalls).isEqualTo(1);
        ManualLapTimeId repeated = ManualLapTimeId.newId();
        assertThatThrownBy(() -> record.recordAll(List.of(
            command(repeated, ROGER, CORTO, R6, LocalDate.of(2024, 6, 12), 80_000),
            command(repeated, ROGER, CORTO, R6, LocalDate.of(2024, 6, 12), 80_000))))
            .hasMessageContaining("Repeated");
    }

    @Test
    void listsFiltersAndDeletesOnlyOwnTimes() {
        ManualLapTime calafat = record.record(command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 105_300)).time();
        record.record(command(ManualLapTimeId.newId(), ROGER, CORTO, CBR, LocalDate.of(2023, 3, 3), 80_000));
        record.record(command(ManualLapTimeId.newId(), OTHER, CORTO, R6, LocalDate.of(2025, 3, 3), 80_000));

        assertThat(manage.list(new ListManualLapTimesQuery(ROGER, null, null))).extracting(ManualLapTime::date)
            .containsExactly(LocalDate.of(2024, 5, 12), LocalDate.of(2023, 3, 3));
        assertThat(manage.list(new ListManualLapTimesQuery(ROGER, null, CBR.motorbikeId()))).hasSize(1);
        assertThatThrownBy(() -> manage.get(OTHER, calafat.id())).isInstanceOf(ManualLapTimeNotFoundException.class);
        assertThatThrownBy(() -> manage.delete(OTHER, calafat.id())).isInstanceOf(ManualLapTimeNotFoundException.class);

        manage.delete(ROGER, calafat.id());

        assertThat(manage.list(new ListManualLapTimesQuery(ROGER, LAYOUT.layoutId(), null))).isEmpty();
    }

    @Test
    void previewsTheNotesOfTheDesign() {
        ManualTimesImportPreview result = preview.preview(ManualTimeNotesParserTest.NOTES);

        assertThat(result.lines()).hasSize(4);
        assertThat(result.readyCount()).isEqualTo(3);
        assertThat(result.lines().get(0).match().layoutRef()).get().extracting(r -> r.layoutName()).isEqualTo("Principal");
        assertThat(result.lines().get(1).parsed().trackCondition()).isEqualTo(TrackCondition.WET);
        assertThat(result.lines().get(2).match().layout().name()).isEqualTo("Nacional");
        ManualTimesImportPreview.Line alcarras = result.lines().get(3);
        assertThat(alcarras.status()).isEqualTo(ManualTimesImportPreview.Status.NEEDS_REVIEW);
        assertThat(alcarras.match().issue()).isEqualTo(CatalogMatcher.Issue.TRACK_AMBIGUOUS);
        assertThat(manualTimes.store).isEmpty();
    }

    @Test
    void previewRetriesWithoutTheLastWordAndReportsUnknownCircuits() {
        ManualTimesImportPreview result = preview.preview("Castellolí corto 2/6/24 1:14,22\nCircuito Inventado 2/6/24 1:14,22");

        assertThat(result.lines().get(0).match().venue().name()).isEqualTo("Parcmotor Castellolí");
        assertThat(result.lines().get(1).circuitNotFound()).isTrue();
        assertThat(result.lines().get(1).status()).isEqualTo(ManualTimesImportPreview.Status.NEEDS_REVIEW);
    }

    @Test
    void recordsKeepGpsAndManualApartAndExistWithOnlyManualTimes() {
        Session gps = recordSession.record(new RecordSessionCommand(SessionId.newId(), ROGER, LAYOUT.venueId(), LAYOUT.layoutId(),
            R6.motorbikeId(), "Tanda", Conditions.UNKNOWN, null, ride(3, PROFILE, 1, 2, false, 1))).session();
        // un manual más rápido que el GPS (crono aproximado) no le quita el récord a la sesión
        ManualLapTime fastManual = record.record(command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2024, 5, 12), 10_000)).time();
        record.record(command(ManualLapTimeId.newId(), ROGER, LAYOUT.layoutId(), R6, LocalDate.of(2023, 3, 3), 109_800));
        record.record(command(ManualLapTimeId.newId(), ROGER, CORTO, CBR, LocalDate.of(2023, 3, 3), 80_000));

        List<PersonalRecord> mine = records.records(ROGER, null, null);

        assertThat(mine).hasSize(2);
        PersonalRecord principal = records.records(ROGER, LAYOUT.layoutId(), R6.motorbikeId()).get(0);
        assertThat(principal.bestLap()).get().extracting(PersonalRecord.RecordLap::sessionId).isEqualTo(gps.id());
        assertThat(principal.idealLap()).isPresent();
        assertThat(principal.bestManual()).get().extracting(PersonalRecord.ManualBest::id).isEqualTo(fastManual.id());
        assertThat(principal.manualTimeCount()).isEqualTo(2);
        assertThat(principal.evolution()).hasSize(3);
        assertThat(principal.evolution().get(0)).isInstanceOf(ManualPoint.class);
        assertThat(principal.evolution().get(2)).isInstanceOf(SessionBest.class);
        assertThat(listSessions.get(ROGER, gps.id()).personalBest()).isTrue();

        PersonalRecord corto = records.records(ROGER, CORTO, null).get(0);
        assertThat(corto.bestLap()).isEmpty();
        assertThat(corto.idealLap()).isEmpty();
        assertThat(corto.motorbike().displayName()).isEqualTo("Honda CBR600");
        assertThat(corto.layout().layoutName()).isEqualTo("Corto");
    }

    static RecordManualLapTimeCommand command(ManualLapTimeId id, RiderId rider, LayoutId layout, MotorbikeSnapshot motorbike, LocalDate date, long millis) {
        return new RecordManualLapTimeCommand(id, rider, LAYOUT.venueId(), layout, motorbike.motorbikeId(),
            new ManualLapEntry(date, millis, null, TimeSource.LAPTIMER, null, null, null, null, null));
    }

    /** Búsqueda como la de circuits: alguna palabra del texto está en el nombre del complejo. */
    static final class FakeCatalog implements VenueCatalogPort {
        final List<CatalogVenue> venues;
        int findVenueCalls;

        FakeCatalog(List<CatalogVenue> venues) {
            this.venues = venues;
        }

        @Override
        public Optional<CatalogVenue> findVenue(VenueId venueId) {
            findVenueCalls++;
            return venues.stream().filter(v -> v.id().equals(venueId)).findFirst();
        }

        @Override
        public List<CatalogVenue> search(String text, int limit) {
            List<String> words = normalize(text);
            return venues.stream()
                .filter(v -> normalize(v.name()).containsAll(words))
                .limit(limit)
                .toList();
        }

        private static List<String> normalize(String text) {
            String plain = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
            return List.of(plain.toLowerCase().replaceAll("[^a-z0-9]+", " ").strip().split(" "));
        }
    }

    static final class InMemoryManualLapTimes implements ManualLapTimeRepositoryPort {
        final Map<ManualLapTimeId, ManualLapTime> store = new HashMap<>();

        @Override
        public void save(ManualLapTime time) {
            store.put(time.id(), time);
        }

        @Override
        public Optional<ManualLapTime> findById(ManualLapTimeId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<ManualLapTime> findByRider(RiderId riderId) {
            List<ManualLapTime> result = new ArrayList<>(store.values().stream().filter(t -> t.isOwnedBy(riderId)).toList());
            result.sort(Comparator.comparing(ManualLapTime::date).reversed());
            return result;
        }

        @Override
        public void delete(ManualLapTimeId id) {
            store.remove(id);
        }
    }
}
