package com.ridei.circuits.domain.model;

/** Identificador del lugar en un proveedor de mapas (Google Places, Mapbox…), tal como lo envía el cliente. */
public record ExternalPlaceId(String value) {

    public static final int MAX_LENGTH = 255;

    public ExternalPlaceId {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("External place id must not be blank");
        value = value.trim();
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("External place id must be at most " + MAX_LENGTH + " characters");
        }
    }

    /** Conversión tolerante a nulos: un id ausente o en blanco equivale a no tenerlo. */
    public static ExternalPlaceId ofNullable(String value) {
        return value == null || value.isBlank() ? null : new ExternalPlaceId(value);
    }
}
