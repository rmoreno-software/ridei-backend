package com.ridei.garage.domain.event;

import java.time.Instant;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

/**
 * Una moto se ha eliminado del garaje. Lo que vive fuera de la base de datos (las fotos en
 * el storage) se limpia al recibir este evento, una vez confirmada la transacción.
 */
public record MotorbikeDeletedEvent(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    Instant occurredAt
) {
    public MotorbikeDeletedEvent {
        if (ownerId == null) throw new IllegalArgumentException("Owner is required");
        if (motorbikeId == null) throw new IllegalArgumentException("Motorbike id is required");
        if (occurredAt == null) throw new IllegalArgumentException("Occurred at is required");
    }

    public static MotorbikeDeletedEvent of(OwnerId ownerId, MotorbikeId motorbikeId) {
        return new MotorbikeDeletedEvent(ownerId, motorbikeId, Instant.now());
    }
}
