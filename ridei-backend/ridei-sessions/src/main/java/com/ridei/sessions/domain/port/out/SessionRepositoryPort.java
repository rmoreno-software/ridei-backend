package com.ridei.sessions.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;

public interface SessionRepositoryPort {

    void save(Session session);

    Optional<Session> findById(SessionId id);

    /** Todas las sesiones del piloto, de la más reciente a la más antigua. */
    List<Session> findByRider(RiderId riderId);

    void delete(SessionId id);
}
