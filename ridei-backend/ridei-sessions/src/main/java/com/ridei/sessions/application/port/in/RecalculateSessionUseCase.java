package com.ridei.sessions.application.port.in;

import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;

/** Vuelve a cronometrar una sesión con la geometría actual del catálogo (meta corregida…). */
public interface RecalculateSessionUseCase {
    Session recalculate(RiderId riderId, SessionId sessionId);
}
