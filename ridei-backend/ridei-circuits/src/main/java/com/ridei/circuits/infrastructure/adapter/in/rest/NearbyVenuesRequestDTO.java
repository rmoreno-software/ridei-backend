package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.application.FindNearbyVenuesQuery;
import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.PageRequest;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Parámetros de {@code GET /venues/nearby}. Radio por defecto: 50 km. */
public record NearbyVenuesRequestDTO(
    @NotNull(message = "lat is required")
    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    Double lat,

    @NotNull(message = "lon is required")
    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    Double lon,

    @Positive(message = "Radius must be positive")
    @Max(value = 500, message = "Radius must be at most 500 km")
    Double radiusKm,

    @Min(value = 0, message = "Page must be zero or positive")
    Integer page,

    @Min(value = 1, message = "Size must be between 1 and 100")
    @Max(value = 100, message = "Size must be between 1 and 100")
    Integer size
) {
    static final double DEFAULT_RADIUS_KM = 50;

    public FindNearbyVenuesQuery toQuery() {
        return new FindNearbyVenuesQuery(
            GeoPoint.of(lat, lon),
            Distance.searchRadiusOfKilometers(radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM),
            PageRequest.of(page != null ? page : 0, size != null ? size : SearchVenuesRequestDTO.DEFAULT_SIZE)
        );
    }
}
