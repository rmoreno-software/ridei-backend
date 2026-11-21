package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.Session;

/** @param created {@code false} si la sesión ya se había subido (reintento idempotente) */
public record RecordSessionResult(Session session, boolean created) {}
