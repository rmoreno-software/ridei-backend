package com.ridei.circuits.infrastructure.adapter.in.rest;

import java.util.UUID;

import com.ridei.circuits.application.VenueDetail;

/** Si {@code redirectedFromMerged}, {@code requestedId} está fusionado y {@code venue} es el canónico. */
public record VenueDetailResponseDTO(UUID requestedId, boolean redirectedFromMerged, VenueResponseDTO venue) {

    public static VenueDetailResponseDTO fromDetail(VenueDetail detail) {
        return new VenueDetailResponseDTO(
            detail.requestedId().value(),
            detail.redirectedFromMerged(),
            VenueResponseDTO.fromDomain(detail.venue())
        );
    }
}
