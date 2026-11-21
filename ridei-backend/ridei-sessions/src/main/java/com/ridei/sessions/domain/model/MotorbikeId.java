package com.ridei.sessions.domain.model;

import java.util.UUID;

public record MotorbikeId(UUID value) {
    public MotorbikeId {
        if (value == null) throw new IllegalArgumentException("Motorbike id is required");
    }

    public static MotorbikeId of(String value) {
        try {
            return new MotorbikeId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Motorbike id must be a UUID");
        }
    }

    public static MotorbikeId of(UUID value) {
        return new MotorbikeId(value);
    }
}
