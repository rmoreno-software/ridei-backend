package com.ridei.garage.domain.model;

import java.util.UUID;

public record OwnerId(UUID value) {
    public OwnerId {
        if (value == null) throw new IllegalArgumentException();
    }

    public static OwnerId of(String value) {
        return new OwnerId(UUID.fromString(value));
    }
}
