package com.ridei.identity.domain.model;

import java.util.UUID;

public record UserId(UUID value) {
    public static UserId newId() {
        return new UserId(UUID.randomUUID());
    }

    public static UserId of(String value) {
        return new UserId(UUID.fromString(value));
    }
}
