package com.ridei.sessions.domain.port.out;

import java.util.Optional;

import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.Telemetry;

/** Telemetría bruta de cada sesión, guardada aparte porque es grande. */
public interface TelemetryRepositoryPort {

    void save(SessionId sessionId, Telemetry telemetry);

    Optional<Telemetry> findBySessionId(SessionId sessionId);
}
