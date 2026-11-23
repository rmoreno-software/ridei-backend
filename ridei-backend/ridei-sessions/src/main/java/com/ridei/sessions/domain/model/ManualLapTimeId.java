package com.ridei.sessions.domain.model;

import java.util.UUID;

/** Lo genera la app al abrir el formulario: reintentar el guardado no duplica el tiempo. */
public record ManualLapTimeId(UUID value) {
    public ManualLapTimeId {
        if (value == null) throw new IllegalArgumentException("Manual lap time id is required");
    }

    public static ManualLapTimeId newId() {
        return new ManualLapTimeId(UUID.randomUUID());
    }

    public static ManualLapTimeId of(String value) {
        try {
            return new ManualLapTimeId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Manual lap time id must be a UUID");
        }
    }

    public static ManualLapTimeId of(UUID value) {
        return new ManualLapTimeId(value);
    }
}
