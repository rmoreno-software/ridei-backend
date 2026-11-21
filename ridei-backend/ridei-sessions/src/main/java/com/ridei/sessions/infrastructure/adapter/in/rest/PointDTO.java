package com.ridei.sessions.infrastructure.adapter.in.rest;

import com.ridei.sessions.domain.model.GeoPoint;

/** Punto WGS84, mismos nombres que en ridei-circuits. */
public record PointDTO(double latitude, double longitude) {
    public static PointDTO fromDomain(GeoPoint point) {
        return new PointDTO(point.latitude(), point.longitude());
    }
}
