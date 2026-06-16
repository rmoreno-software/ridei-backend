package com.ridei.identity.domain.event;

import java.time.Instant;

import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserRole;

public record UserRegisteredEvent(
    UserId userId,
    UserRole userRole,
    Instant ocurredAt
) {}
