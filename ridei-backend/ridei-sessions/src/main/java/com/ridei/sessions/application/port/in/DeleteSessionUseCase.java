package com.ridei.sessions.application.port.in;

import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

public interface DeleteSessionUseCase {
    void delete(RiderId riderId, SessionId sessionId);
}
