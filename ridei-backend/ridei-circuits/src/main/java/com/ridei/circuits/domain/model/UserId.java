package com.ridei.circuits.domain.model;

import java.util.UUID;

public record UserId(UUID value) {

    public UserId {
        if (value == null) throw new IllegalArgumentException("User id is required");
    }

    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
