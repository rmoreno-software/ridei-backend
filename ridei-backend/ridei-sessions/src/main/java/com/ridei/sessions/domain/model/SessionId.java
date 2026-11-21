package com.ridei.sessions.domain.model;

import java.util.UUID;

public record SessionId(UUID value) {
    public SessionId {
        if (value == null) throw new IllegalArgumentException("Session id is required");
    }

    public static SessionId newId() {
        return new SessionId(UUID.randomUUID());
    }

    public static SessionId of(String value) {
        try {
            return new SessionId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Session id must be a UUID");
        }
    }

    public static SessionId of(UUID value) {
        return new SessionId(value);
    }
}
