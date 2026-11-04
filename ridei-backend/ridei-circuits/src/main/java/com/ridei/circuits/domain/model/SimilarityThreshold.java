package com.ridei.circuits.domain.model;

/** Similitud mínima (trigramas, 0..1) a partir de la cual dos nombres se consideran el mismo. */
public record SimilarityThreshold(double value) {

    /** Valor por defecto; equivale a {@code circuits.deduplication.min-similarity}. */
    public static final SimilarityThreshold DEFAULT = new SimilarityThreshold(0.4);

    public SimilarityThreshold {
        if (value <= 0 || value > 1) throw new IllegalArgumentException("Similarity threshold must be in (0, 1]");
    }

    public boolean isReachedBy(double similarity) {
        return similarity >= value;
    }
}
