package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.VenueCorrection;
import com.ridei.circuits.domain.model.VenueName;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** PATCH: los campos ausentes no se modifican. */
public record UpdateVenueRequestDTO(
    @Size(min = 1, max = 150, message = "Venue name must be between 1 and 150 characters")
    String name,

    @Pattern(regexp = "[A-Za-z]{2}", message = "Country code must be an ISO 3166-1 alpha-2 code")
    String countryCode,

    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    Double latitude,

    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    Double longitude,

    @Size(min = 1, max = 255, message = "External place id must be between 1 and 255 characters")
    String externalPlaceId
) {
    @AssertTrue(message = "latitude and longitude must be given together")
    public boolean isLocationComplete() {
        return (latitude == null) == (longitude == null);
    }

    public VenueCorrection toCorrection() {
        return new VenueCorrection(
            name != null ? VenueName.of(name) : null,
            countryCode != null ? CountryCode.of(countryCode) : null,
            latitude != null ? GeoPoint.of(latitude, longitude) : null,
            ExternalPlaceId.ofNullable(externalPlaceId)
        );
    }
}
