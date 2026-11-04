package com.ridei.circuits.domain.exception;

import java.util.List;

import com.ridei.circuits.domain.model.VenueCandidate;

/** Hay venues cercanos con nombre similar; el usuario debe confirmar que no es ninguno de ellos. */
public class PossibleDuplicateVenueException extends RuntimeException {

    private final List<VenueCandidate> candidates;

    public PossibleDuplicateVenueException(List<VenueCandidate> candidates) {
        super("A similar venue already exists nearby");
        this.candidates = List.copyOf(candidates);
    }

    public List<VenueCandidate> getCandidates() {
        return candidates;
    }
}
