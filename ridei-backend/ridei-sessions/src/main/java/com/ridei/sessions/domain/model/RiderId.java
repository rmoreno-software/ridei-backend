package com.ridei.sessions.domain.model;

import java.util.UUID;

public record RiderId(UUID value) {
    public RiderId {
        if (value == null) throw new IllegalArgumentException("Rider id is required");
    }

    public static RiderId of(String value) {
        try {
            return new RiderId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Rider id must be a UUID");
        }
    }

    public static RiderId of(UUID value) {
        return new RiderId(value);
    }
}
