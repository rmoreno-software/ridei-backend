package com.ridei.garage.domain.model;

import java.util.UUID;

public record MotorbikeId(UUID value) {
    public MotorbikeId {
        if (value == null) throw new IllegalArgumentException("Motorbike id is required");
    }

    public static MotorbikeId newId() {
        return new MotorbikeId(UUID.randomUUID());
    }

    public static MotorbikeId of(String value) {
        return new MotorbikeId(UUID.fromString(value));
    }
}
