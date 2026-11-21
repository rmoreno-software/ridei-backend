package com.ridei.sessions.domain.model;

/**
 * Moto del garaje ({@code ridei-garage}) con la que se rodó la sesión. El tipo de sesión
 * (asfalto, mixto, off-road) se hereda de su categoría.
 */
public record MotorbikeSnapshot(
    MotorbikeId motorbikeId,
    String displayName,
    String category,
    String categoryGroup
) {
    public MotorbikeSnapshot {
        if (motorbikeId == null) throw new IllegalArgumentException("Motorbike id is required");
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("Motorbike name is required");
    }
}
