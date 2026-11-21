package com.ridei.sessions.application.port.in;

import com.ridei.sessions.domain.model.CornerAnalysis;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

public interface GetCornerAnalysisUseCase {
    CornerAnalysis analyze(RiderId riderId, SessionId sessionId);
}
