package com.ridei.sessions.application;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.GetCornerAnalysisUseCase;
import com.ridei.sessions.application.port.in.GetLapTraceUseCase;
import com.ridei.sessions.application.port.in.GetReferenceLapUseCase;
import com.ridei.sessions.domain.exception.LapNotFoundException;
import com.ridei.sessions.domain.exception.NoValidLapsException;
import com.ridei.sessions.domain.model.CornerAnalysis;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapComparison;
import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;
import com.ridei.sessions.domain.service.CornerAnalyzer;
import com.ridei.sessions.domain.service.TimedRun;

/**
 * Análisis bajo demanda a partir de la telemetría guardada: trazas, comparaciones, curvas y la
 * vuelta de referencia del HUD. Nada de esto se persiste: siempre sale del mismo cronometraje que
 * las vueltas guardadas.
 */
public class LapAnalysisService implements GetLapTraceUseCase, GetCornerAnalysisUseCase, GetReferenceLapUseCase {

    private final SessionRepositoryPort sessionRepository;
    private final TelemetryRepositoryPort telemetryRepository;

    public LapAnalysisService(SessionRepositoryPort sessionRepository, TelemetryRepositoryPort telemetryRepository) {
        this.sessionRepository = sessionRepository;
        this.telemetryRepository = telemetryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public LapTrace trace(RiderId riderId, SessionId sessionId, int lapNumber) {
        return run(riderId, sessionId).trace(lapNumber).orElseThrow(LapNotFoundException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public LapTrace idealTrace(RiderId riderId, SessionId sessionId) {
        return run(riderId, sessionId).idealTrace().orElseThrow(NoValidLapsException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public LapComparison compare(RiderId riderId, SessionId sessionId, int lapNumber, LapReference reference) {
        TimedRun run = run(riderId, sessionId);
        LapTrace lap = run.trace(lapNumber).orElseThrow(LapNotFoundException::new);
        LapTrace against = switch (reference) {
            case LapReference.Ideal ideal -> run.idealTrace().orElseThrow(NoValidLapsException::new);
            case LapReference.BestLap best -> run.trace(
                run.timing().bestLap().orElseThrow(NoValidLapsException::new).number()).orElseThrow();
            case LapReference.OtherLap other -> run.trace(other.number()).orElseThrow(LapNotFoundException::new);
        };
        return LapComparison.of(lap, against);
    }

    @Override
    @Transactional(readOnly = true)
    public CornerAnalysis analyze(RiderId riderId, SessionId sessionId) {
        return CornerAnalyzer.analyze(run(riderId, sessionId)).orElseThrow(NoValidLapsException::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReferenceLap> reference(RiderId riderId, LayoutId layoutId, MotorbikeId motorbikeId) {
        Optional<Session> holder = sessionRepository.findByRider(riderId).stream()
            .filter(s -> s.layout().layoutId().equals(layoutId) && s.motorbike().motorbikeId().equals(motorbikeId))
            .filter(s -> s.bestLap().isPresent())
            .min(Comparator.comparingLong((Session s) -> s.bestLap().map(Lap::durationMillis).orElseThrow()));
        return holder.map(session -> {
            TimedRun run = session.analyze(OwnedSessions.telemetryOf(telemetryRepository, session));
            return new ReferenceLap(session, run.trace(session.bestLap().orElseThrow().number()).orElseThrow());
        });
    }

    private TimedRun run(RiderId riderId, SessionId sessionId) {
        return OwnedSessions.analyze(sessionRepository, telemetryRepository, riderId, sessionId);
    }
}
