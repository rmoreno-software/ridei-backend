package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.application.ProposeVenueCommand;
import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.VenueName;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Propuesta de un venue con su primer track y, opcionalmente, su primer layout. El proponente
 * sale del JWT, nunca del cuerpo.
 */
public record ProposeVenueRequestDTO(
    @NotBlank(message = "Venue name is required")
    @Size(max = 150, message = "Venue name must be at most 150 characters")
    String name,

    @NotBlank(message = "Country code is required")
    @Pattern(regexp = "[A-Za-z]{2}", message = "Country code must be an ISO 3166-1 alpha-2 code")
    String countryCode,

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
    Double latitude,

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
    Double longitude,

    @Size(max = 255, message = "External place id must be at most 255 characters")
    String externalPlaceId,

    @NotNull(message = "The first track is required")
    @Valid
    TrackRequestDTO track,

    @Valid
    LayoutRequestDTO layout,

    Boolean confirmedNotDuplicate
) {
    public ProposeVenueCommand toCommand(UserId proposedBy) {
        return new ProposeVenueCommand(
            proposedBy,
            VenueName.of(name),
            CountryCode.of(countryCode),
            GeoPoint.of(latitude, longitude),
            ExternalPlaceId.ofNullable(externalPlaceId),
            track.toSpec(),
            layout != null ? layout.toSpec() : null,
            Boolean.TRUE.equals(confirmedNotDuplicate)
        );
    }
}
