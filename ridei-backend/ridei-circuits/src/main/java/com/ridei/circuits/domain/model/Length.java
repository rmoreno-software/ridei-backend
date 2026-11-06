package com.ridei.circuits.domain.model;

/** Longitud de un trazado, en metros enteros. */
public record Length(int meters) {

    public Length {
        if (meters <= 0) throw new IllegalArgumentException("Length must be greater than 0 meters");
    }

    public static Length ofMeters(int meters) {
        return new Length(meters);
    }

    /** Conversión tolerante a nulos: una longitud aún no calculada es {@code null}. */
    public static Length ofNullable(Integer meters) {
        return meters == null ? null : new Length(meters);
    }

    public boolean isWithinToleranceOf(Length other, int toleranceMeters) {
        return Math.abs(meters - other.meters) <= toleranceMeters;
    }
}
