package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.application.ProposeVenueResult;

/** {@code outcome}: CREATED (201) o EXISTING_BY_EXTERNAL_PLACE_ID (200). */
public record ProposeVenueResponseDTO(ProposeVenueResult.Outcome outcome, VenueResponseDTO venue) {

    public static ProposeVenueResponseDTO fromResult(ProposeVenueResult result) {
        return new ProposeVenueResponseDTO(result.outcome(), VenueResponseDTO.fromDomain(result.venue()));
    }
}
