package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.SessionSummary;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

public interface GetSessionUseCase {
    SessionSummary get(RiderId riderId, SessionId sessionId);
}
