package com.ridei.circuits.infrastructure.adapter.in.rest.exception;

import java.util.UUID;

import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.VenueCandidate;

/** Elemento existente que podría ser el que el usuario intenta crear. */
public record CandidateDTO(
    UUID id,
    String name,
    Long distanceMeters,
    double similarity,
    CatalogStatus status
) {
    public static CandidateDTO fromVenue(VenueCandidate candidate) {
        return new CandidateDTO(
            candidate.id().value(),
            candidate.name().value(),
            Math.round(candidate.distance().meters()),
            round(candidate.similarity()),
            candidate.status()
        );
    }

    public static CandidateDTO fromTrack(Track track, double similarity) {
        return new CandidateDTO(track.getId().value(), track.getName().value(), null, round(similarity), track.getStatus());
    }

    public static CandidateDTO fromLayout(Layout layout, double similarity) {
        return new CandidateDTO(layout.getId().value(), layout.getName().value(), null, round(similarity), layout.getStatus());
    }

    private static double round(double similarity) {
        return Math.round(similarity * 1000) / 1000.0;
    }
}
