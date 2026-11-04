package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Venue;

/** Solo incluye los tracks activos. */
public record VenueResponseDTO(
    UUID id,
    String name,
    String countryCode,
    double latitude,
    double longitude,
    String externalPlaceId,
    CatalogStatus status,
    List<TrackResponseDTO> tracks,
    Instant createdAt,
    Instant updatedAt
) {
    public static VenueResponseDTO fromDomain(Venue venue) {
        return new VenueResponseDTO(
            venue.getId().value(),
            venue.getName().value(),
            venue.getCountryCode().value(),
            venue.getLocation().latitude(),
            venue.getLocation().longitude(),
            venue.getExternalPlaceId() != null ? venue.getExternalPlaceId().value() : null,
            venue.getStatus(),
            venue.activeTracks().stream().map(TrackResponseDTO::fromDomain).toList(),
            venue.getCreatedAt(),
            venue.getUpdatedAt()
        );
    }
}
