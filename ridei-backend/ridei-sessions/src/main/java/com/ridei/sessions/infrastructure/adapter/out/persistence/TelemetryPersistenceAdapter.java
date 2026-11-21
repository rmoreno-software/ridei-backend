package com.ridei.sessions.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;

@Component
class TelemetryPersistenceAdapter implements TelemetryRepositoryPort {

    private final TelemetryJpaRepository repository;

    TelemetryPersistenceAdapter(TelemetryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(SessionId sessionId, Telemetry telemetry) {
        TelemetryJpaEntity entity = new TelemetryJpaEntity();
        entity.sessionId = sessionId.value();
        entity.encoding = TelemetryCodec.ENCODING;
        entity.sampleCount = telemetry.samples().size();
        entity.data = TelemetryCodec.encode(telemetry.samples());
        repository.save(entity);
    }

    @Override
    public Optional<Telemetry> findBySessionId(SessionId sessionId) {
        return repository.findById(sessionId.value())
            .map(e -> Telemetry.reconstitute(TelemetryCodec.decode(e.encoding, e.data)));
    }
}
