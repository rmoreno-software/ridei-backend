package com.ridei.identity.domain.event;

import java.time.Instant;

import com.ridei.identity.domain.model.UserId;

public record UserGoogleAccountLinkedEvent(
    UserId userId,
    String googleId,
    Instant occurredAt
) implements DomainEvent {}
