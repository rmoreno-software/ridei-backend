package com.ridei.landing.domain.model;

import java.time.Instant;
import java.util.UUID;

public class WaitlistEntry {

    private final UUID id;
    private final Email email;
    private final Instant joinedAt;

    private WaitlistEntry(UUID id, Email email, Instant joinedAt) {
        this.id = id;
        this.email = email;
        this.joinedAt = joinedAt;
    }

    public static WaitlistEntry join(Email email) {
        return new WaitlistEntry(
            UUID.randomUUID(),
            email,
            Instant.now()
        );
    }

    public UUID getId() { return id; }
    public Email getEmail() { return email; }
    public Instant getJoinedAt() { return joinedAt; }
    
}
