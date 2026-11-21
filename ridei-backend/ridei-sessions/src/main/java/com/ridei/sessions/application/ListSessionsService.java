package com.ridei.sessions.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.sessions.application.port.in.GetSessionUseCase;
import com.ridei.sessions.application.port.in.ListSessionsUseCase;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.port.out.SessionRepositoryPort;
import com.ridei.sessions.domain.service.PersonalRecords;

/** Historial de sesiones (home) y detalle de una, marcando las que tienen el récord personal. */
public class ListSessionsService implements ListSessionsUseCase, GetSessionUseCase {

    private final SessionRepositoryPort sessionRepository;

    public ListSessionsService(SessionRepositoryPort sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SessionSummary> list(ListSessionsQuery query) {
        List<Session> all = sessionRepository.findByRider(query.riderId());
        Map<PersonalRecords.Key, SessionId> recordHolders = recordHolders(all);
        return all.stream()
            .filter(s -> query.motorbikeId() == null || s.motorbike().motorbikeId().equals(query.motorbikeId()))
            .filter(s -> query.layoutId() == null || s.layout().layoutId().equals(query.layoutId()))
            .filter(s -> query.categoryGroup() == null || query.categoryGroup().equalsIgnoreCase(s.motorbike().categoryGroup()))
            .map(s -> new SessionSummary(s, s.id().equals(recordHolders.get(PersonalRecords.Key.of(s)))))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SessionSummary get(RiderId riderId, SessionId sessionId) {
        Session session = OwnedSessions.require(sessionRepository, riderId, sessionId);
        SessionId holder = recordHolders(sessionRepository.findByRider(riderId)).get(PersonalRecords.Key.of(session));
        return new SessionSummary(session, session.id().equals(holder));
    }

    private static Map<PersonalRecords.Key, SessionId> recordHolders(List<Session> sessions) {
        return PersonalRecords.of(sessions).stream().collect(Collectors.toMap(
            r -> new PersonalRecords.Key(r.layout().layoutId(), r.motorbike().motorbikeId()),
            r -> r.bestLap().sessionId(),
            (a, b) -> a));
    }
}
