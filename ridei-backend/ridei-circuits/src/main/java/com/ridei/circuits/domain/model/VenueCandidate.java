package com.ridei.circuits.domain.model;

/** Venue existente que podría ser el mismo que se está proponiendo. */
public record VenueCandidate(
    VenueId id,
    VenueName name,
    CatalogStatus status,
    Distance distance,
    double similarity
) {
    public VenueCandidate {
        if (id == null || name == null || status == null || distance == null) {
            throw new IllegalArgumentException("Candidate id, name, status and distance are required");
        }
    }
}
