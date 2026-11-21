package com.ridei.sessions.application;

import com.ridei.sessions.domain.exception.SessionNotFoundException;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;
import com.ridei.sessions.domain.service.TimedRun;

/** Anti-IDOR: una sesión de otro piloto es indistinguible de una que no existe (404). */
final class OwnedSessions {
    private OwnedSessions() {}

    static Session require(SessionRepositoryPort repository, RiderId riderId, SessionId sessionId) {
        return repository.findById(sessionId)
            .filter(s -> s.isOwnedBy(riderId))
            .orElseThrow(SessionNotFoundException::new);
    }

    static Telemetry telemetryOf(TelemetryRepositoryPort telemetryRepository, Session session) {
        return telemetryRepository.findBySessionId(session.id())
            .orElseThrow(() -> new IllegalStateException("Telemetry missing for session " + session.id().value()));
    }

    static TimedRun analyze(SessionRepositoryPort repository, TelemetryRepositoryPort telemetryRepository, RiderId riderId, SessionId sessionId) {
        Session session = require(repository, riderId, sessionId);
        return session.analyze(telemetryOf(telemetryRepository, session));
    }
}
