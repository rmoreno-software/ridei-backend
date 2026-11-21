package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.RiderId;
import com.ridei.sessions.domain.model.SessionId;

/** Campos {@code null} = sin cambios. */
public record EditSessionCommand(RiderId riderId, SessionId sessionId, String name, Conditions conditions, String notes) {}
