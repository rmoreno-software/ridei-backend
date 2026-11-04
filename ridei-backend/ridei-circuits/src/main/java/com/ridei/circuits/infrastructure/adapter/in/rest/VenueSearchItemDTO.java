package com.ridei.circuits.infrastructure.adapter.in.rest;

import com.ridei.circuits.domain.model.VenueSearchHit;

/** {@code distanceMeters} solo si se buscó por cercanía; {@code similarity} solo si se buscó texto. */
public record VenueSearchItemDTO(VenueResponseDTO venue, Double distanceMeters, Double similarity) {

    public static VenueSearchItemDTO fromDomain(VenueSearchHit hit) {
        return new VenueSearchItemDTO(
            VenueResponseDTO.fromDomain(hit.venue()),
            hit.distance() != null ? Math.round(hit.distance().meters() * 10) / 10.0 : null,
            hit.similarity()
        );
    }
}
