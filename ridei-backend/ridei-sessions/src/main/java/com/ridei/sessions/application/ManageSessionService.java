package com.ridei.sessions.application;

import java.time.Clock;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.DeleteSessionUseCase;
import com.ridei.sessions.application.port.in.EditSessionUseCase;
import com.ridei.sessions.application.port.in.RecalculateSessionUseCase;
import com.ridei.sessions.domain.exception.LayoutNotFoundException;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.port.out.LayoutCatalogPort;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.port.out.TelemetryRepositoryPort;

/** Editar, recalcular y borrar una sesión propia. */
public class ManageSessionService implements EditSessionUseCase, RecalculateSessionUseCase, DeleteSessionUseCase {

    private final SessionRepositoryPort sessionRepository;
    private final TelemetryRepositoryPort telemetryRepository;
    private final LayoutCatalogPort layoutCatalog;
    private final Clock clock;

    public ManageSessionService(
        SessionRepositoryPort sessionRepository,
        TelemetryRepositoryPort telemetryRepository,
        LayoutCatalogPort layoutCatalog,
        Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.telemetryRepository = telemetryRepository;
        this.layoutCatalog = layoutCatalog;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Session edit(EditSessionCommand command) {
        Session session = OwnedSessions.require(sessionRepository, command.riderId(), command.sessionId());
        session.editDetails(command.name(), command.conditions(), command.notes(), clock.instant());
        sessionRepository.save(session);
        return session;
    }

    @Override
    @Transactional
    public Session recalculate(RiderId riderId, SessionId sessionId) {
        Session session = OwnedSessions.require(sessionRepository, riderId, sessionId);
        LayoutSnapshot current = layoutCatalog.findLayout(session.layout().venueId(), session.layout().layoutId())
            .orElseThrow(LayoutNotFoundException::new);
        session.retime(current, OwnedSessions.telemetryOf(telemetryRepository, session), clock.instant());
        sessionRepository.save(session);
        return session;
    }

    @Override
    @Transactional
    public void delete(RiderId riderId, SessionId sessionId) {
        OwnedSessions.require(sessionRepository, riderId, sessionId);
        sessionRepository.delete(sessionId);
    }
}
