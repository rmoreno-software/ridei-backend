package com.ridei.circuits.domain.model;

/** Número de curvas de un trazado. */
public record Turns(int count) {

    public Turns {
        if (count < 0) throw new IllegalArgumentException("Turns must be zero or positive");
    }

    /** Conversión tolerante a nulos: un número de curvas desconocido es {@code null}. */
    public static Turns ofNullable(Integer count) {
        return count == null ? null : new Turns(count);
    }
}
