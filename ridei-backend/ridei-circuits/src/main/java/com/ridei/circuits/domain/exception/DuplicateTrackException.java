package com.ridei.circuits.domain.exception;

import com.ridei.circuits.domain.model.Track;

/** El venue ya tiene un track activo de la misma disciplina con nombre similar. */
public class DuplicateTrackException extends RuntimeException {

    private final Track existing;
    private final double similarity;

    public DuplicateTrackException(Track existing, double similarity) {
        super("A similar " + existing.getDiscipline() + " track already exists in this venue: " + existing.getName());
        this.existing = existing;
        this.similarity = similarity;
    }

    public Track getExisting() {
        return existing;
    }

    public double getSimilarity() {
        return similarity;
    }
}
