package com.ridei.sessions.application;

import static com.ridei.sessions.domain.CalafatFixture.LAYOUT;
import static com.ridei.sessions.domain.CalafatFixture.R6;
import static com.ridei.sessions.domain.CalafatFixture.ride;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.exception.MotorbikeNotFoundException;
import com.ridei.sessions.domain.exception.SessionIdConflictException;
import com.ridei.sessions.domain.exception.SessionNotFoundException;
import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.model.TrackCondition;
import com.ridei.sessions.domain.model.VenueId;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;

class SessionUseCasesTest {

    static final RiderId ROGER = RiderId.of("11111111-1111-4111-8111-111111111111");
    static final RiderId OTHER = RiderId.of("22222222-2222-4222-8222-222222222222");
    static final DoubleUnaryOperator PROFILE = s -> 26 + 14 * Math.cos(2 * Math.PI * s / 400);
    static final DoubleUnaryOperator FASTER = s -> PROFILE.applyAsDouble(s) * 1.05;

    InMemorySessions sessions;
    InMemoryTelemetry telemetry;
    RecordSessionService record;
    ListSessionsService list;
    ManageSessionService manage;
    LapAnalysisService analysis;

    @BeforeEach
    void setUp() {
        sessions = new InMemorySessions();
        telemetry = new InMemoryTelemetry();
        Clock clock = Clock.fixed(Instant.parse("2026-11-28T18:00:00Z"), ZoneOffset.UTC);
        LayoutCatalogPort catalog = (venue, layout) -> layout.equals(LAYOUT.layoutId()) ? Optional.of(LAYOUT) : Optional.empty();
        // el garaje solo conoce la R6 (en producción responde con las motos del token)
        GaragePort garage = id -> id.equals(R6.motorbikeId()) ? Optional.of(R6) : Optional.empty();
        record = new RecordSessionService(sessions, telemetry, catalog, garage, clock);
        list = new ListSessionsService(sessions);
        manage = new ManageSessionService(sessions, telemetry, catalog, clock);
        analysis = new LapAnalysisService(sessions, telemetry);
    }

    @Test
    void recordsAndTimesAnUploadedSession() {
        RecordSessionResult result = record.record(command(SessionId.newId(), ROGER, PROFILE));

        assertThat(result.created()).isTrue();
        Session session = result.session();
        assertThat(session.timing().validLaps()).hasSize(3);
        assertThat(session.name()).isEqualTo("Tanda tarde");
        assertThat(session.conditions().trackCondition()).isEqualTo(TrackCondition.DRY);
        assertThat(telemetry.store).containsKey(session.id());
    }

    @Test
    void retryingAnUploadReturnsTheSameSessionWithoutDuplicating() {
        SessionId id = SessionId.newId();
        record.record(command(id, ROGER, PROFILE));

        RecordSessionResult retry = record.record(command(id, ROGER, PROFILE));

        assertThat(retry.created()).isFalse();
        assertThat(sessions.store).hasSize(1);
    }

    @Test
    void anotherRiderCannotReuseASessionId() {
        SessionId id = SessionId.newId();
        record.record(command(id, ROGER, PROFILE));

        assertThatThrownBy(() -> record.record(command(id, OTHER, PROFILE))).isInstanceOf(SessionIdConflictException.class);
    }

    @Test
    void rejectsAMotorbikeThatIsNotInTheRidersGarageAndAnUnknownLayout() {
        RecordSessionCommand base = command(SessionId.newId(), ROGER, PROFILE);
        RecordSessionCommand foreignBike = new RecordSessionCommand(base.sessionId(), ROGER, base.venueId(), base.layoutId(),
            MotorbikeId.of("99999999-9999-4999-8999-999999999999"), null, null, null, base.samples());
        RecordSessionCommand unknownLayout = new RecordSessionCommand(base.sessionId(), ROGER, base.venueId(),
            LayoutId.of("99999999-9999-4999-8999-999999999999"), base.motorbikeId(), null, null, null, base.samples());

        assertThatThrownBy(() -> record.record(foreignBike)).isInstanceOf(MotorbikeNotFoundException.class);
        assertThatThrownBy(() -> record.record(unknownLayout)).isInstanceOf(LayoutNotFoundException.class);
        assertThat(sessions.store).isEmpty();
    }

    @Test
    void onlyTheFastestSessionHoldsThePersonalBest() {
        Session slow = record.record(command(SessionId.newId(), ROGER, PROFILE)).session();
        Session fast = record.record(command(SessionId.newId(), ROGER, FASTER)).session();

        List<SessionSummary> mine = list.list(new ListSessionsQuery(ROGER, null, null, null));

        assertThat(mine).hasSize(2);
        assertThat(mine).filteredOn(SessionSummary::personalBest).extracting(s -> s.session().id()).containsExactly(fast.id());
        assertThat(list.get(ROGER, slow.id()).personalBest()).isFalse();
        assertThat(analysis.reference(ROGER, LAYOUT.layoutId(), R6.motorbikeId()).orElseThrow().session().id()).isEqualTo(fast.id());
    }

    @Test
    void sessionsOfAnotherRiderAreInvisible() {
        Session mine = record.record(command(SessionId.newId(), ROGER, PROFILE)).session();

        assertThat(list.list(new ListSessionsQuery(OTHER, null, null, null))).isEmpty();
        assertThatThrownBy(() -> list.get(OTHER, mine.id())).isInstanceOf(SessionNotFoundException.class);
        assertThatThrownBy(() -> analysis.trace(OTHER, mine.id(), 1)).isInstanceOf(SessionNotFoundException.class);
        assertThatThrownBy(() -> manage.delete(OTHER, mine.id())).isInstanceOf(SessionNotFoundException.class);
        assertThat(sessions.store).containsKey(mine.id());
    }

    @Test
    void editsAndRecalculatesOwnSessions() {
        Session session = record.record(command(SessionId.newId(), ROGER, PROFILE)).session();

        Session edited = manage.edit(new EditSessionCommand(ROGER, session.id(), "Tanda 3", null, "Frenar más tarde en T4"));
        Session recalculated = manage.recalculate(ROGER, session.id());

        assertThat(edited.name()).isEqualTo("Tanda 3");
        assertThat(edited.notes()).isEqualTo("Frenar más tarde en T4");
        assertThat(recalculated.timing().laps()).isEqualTo(session.timing().laps());
    }

    @Test
    void comparesALapWithTheIdealLap() {
        Session session = record.record(command(SessionId.newId(), ROGER, PROFILE)).session();

        var comparison = analysis.compare(ROGER, session.id(), 1, new LapReference.Ideal());

        assertThat(comparison.reference().lapNumber()).isZero();
        assertThat(comparison.finalDeltaMillis()).isGreaterThanOrEqualTo(0);
    }

    private static RecordSessionCommand command(SessionId id, RiderId rider, DoubleUnaryOperator profile) {
        return new RecordSessionCommand(id, rider, VenueId.of("10000000-0000-4000-8000-000000000009"), LAYOUT.layoutId(),
            R6.motorbikeId(), "Tanda tarde", new Conditions(24, TrackCondition.DRY), null, ride(3, profile, 1, 2, false, 1));
    }

    static final class InMemorySessions implements SessionRepositoryPort {
        final Map<SessionId, Session> store = new HashMap<>();

        @Override
        public void save(Session session) {
            store.put(session.id(), session);
        }

        @Override
        public Optional<Session> findById(SessionId id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Session> findByRider(RiderId riderId) {
            List<Session> result = new ArrayList<>(store.values().stream().filter(s -> s.isOwnedBy(riderId)).toList());
            result.sort(Comparator.comparing(Session::startedAt).reversed());
            return result;
        }

        @Override
        public void delete(SessionId id) {
            store.remove(id);
        }
    }

    static final class InMemoryTelemetry implements TelemetryRepositoryPort {
        final Map<SessionId, Telemetry> store = new HashMap<>();

        @Override
        public void save(SessionId sessionId, Telemetry value) {
            store.put(sessionId, value);
        }

        @Override
        public Optional<Telemetry> findBySessionId(SessionId sessionId) {
            return Optional.ofNullable(store.get(sessionId));
        }
    }
}
