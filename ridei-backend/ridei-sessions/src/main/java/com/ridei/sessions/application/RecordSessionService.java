package com.ridei.sessions.application;

import java.time.Clock;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.RecordSessionUseCase;
import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.exception.MotorbikeNotFoundException;
import com.ridei.sessions.domain.exception.SessionIdConflictException;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.port.out.GaragePort;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;

/**
 * Sube una tanda grabada: congela el trazado (meta y dibujo) y la moto, cronometra las vueltas y
 * guarda la telemetría bruta para poder recalcular. Idempotente por id de sesión.
 */
public class RecordSessionService implements RecordSessionUseCase {

    private final SessionRepositoryPort sessionRepository;
    private final TelemetryRepositoryPort telemetryRepository;
    private final LayoutCatalogPort layoutCatalog;
    private final GaragePort garage;
    private final Clock clock;

    public RecordSessionService(
        SessionRepositoryPort sessionRepository,
        TelemetryRepositoryPort telemetryRepository,
        LayoutCatalogPort layoutCatalog,
        GaragePort garage,
        Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.telemetryRepository = telemetryRepository;
        this.layoutCatalog = layoutCatalog;
        this.garage = garage;
        this.clock = clock;
    }

    @Override
    @Transactional
    public RecordSessionResult record(RecordSessionCommand command) {
        Optional<Session> existing = sessionRepository.findById(command.sessionId());
        if (existing.isPresent()) {
            if (!existing.get().isOwnedBy(command.riderId())) throw new SessionIdConflictException();
            return new RecordSessionResult(existing.get(), false);
        }

        Telemetry telemetry = Telemetry.recorded(command.samples());
        MotorbikeSnapshot motorbike = garage.findOwnMotorbike(command.motorbikeId())
            .orElseThrow(MotorbikeNotFoundException::new);
        LayoutSnapshot layout = layoutCatalog.findLayout(command.venueId(), command.layoutId())
            .orElseThrow(LayoutNotFoundException::new);

        Session session = Session.record(
            command.sessionId(), command.riderId(), layout, motorbike,
            command.name(), command.conditions(), command.notes(), telemetry, clock.instant());
        sessionRepository.save(session);
        telemetryRepository.save(session.id(), telemetry);
        return new RecordSessionResult(session, true);
    }

}
