package com.ridei.sessions.application.port.in;

import java.util.List;

import com.ridei.sessions.application.ListSessionsQuery;
import com.ridei.sessions.application.SessionSummary;

public interface ListSessionsUseCase {
    List<SessionSummary> list(ListSessionsQuery query);
}
