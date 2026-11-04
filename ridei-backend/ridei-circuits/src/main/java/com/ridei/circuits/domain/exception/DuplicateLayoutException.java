package com.ridei.circuits.domain.exception;

import com.ridei.circuits.domain.model.Layout;

/** El track ya tiene un layout activo equivalente (ver {@link Layout#isEquivalentTo}). */
public class DuplicateLayoutException extends RuntimeException {

    private final Layout existing;
    private final double similarity;

    public DuplicateLayoutException(Layout existing, double similarity) {
        super("An equivalent layout already exists in this track: " + existing.getName());
        this.existing = existing;
        this.similarity = similarity;
    }

    public Layout getExisting() {
        return existing;
    }

    public double getSimilarity() {
        return similarity;
    }
}
