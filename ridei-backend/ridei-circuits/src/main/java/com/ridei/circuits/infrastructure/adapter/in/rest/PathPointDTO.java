package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.GeoPoint;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Punto del dibujo de un trazado (WGS84, grados decimales); mismos nombres que
 * {@code latitude}/{@code longitude} del venue. Tipos envueltos para que un valor ausente
 * llegue como {@code null} y se rechace, en vez de convertirse en 0.
 */
public record PathPointDTO(
    @NotNull(message = "Path point latitude is required")
    @DecimalMin(value = "-90", message = "Path point latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Path point latitude must be between -90 and 90")
    Double latitude,

    @NotNull(message = "Path point longitude is required")
    @DecimalMin(value = "-180", message = "Path point longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Path point longitude must be between -180 and 180")
    Double longitude
) {
    public static PathPointDTO fromDomain(GeoPoint point) {
        return new PathPointDTO(point.latitude(), point.longitude());
    }

    public GeoPoint toDomain() {
        return GeoPoint.of(latitude, longitude);
    }
}
