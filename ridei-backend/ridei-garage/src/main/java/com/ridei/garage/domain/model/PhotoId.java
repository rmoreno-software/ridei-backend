package com.ridei.garage.domain.model;

import java.util.UUID;

public record PhotoId(UUID value) {
    public PhotoId {
        if (value == null) throw new IllegalArgumentException("Photo id is required");
    }

    public static PhotoId newId() {
        return new PhotoId(UUID.randomUUID());
    }

    public static PhotoId of(String value) {
        return new PhotoId(UUID.fromString(value));
    }
}
