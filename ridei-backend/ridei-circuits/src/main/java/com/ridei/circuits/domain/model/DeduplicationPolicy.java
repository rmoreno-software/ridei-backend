package com.ridei.circuits.domain.model;

/**
 * Cuándo un venue propuesto es un posible duplicado de uno existente: a menos de
 * {@code venueRadius} y con similitud de nombre ≥ {@code venueMinSimilarity}.
 */
public record DeduplicationPolicy(Distance venueRadius, SimilarityThreshold venueMinSimilarity) {

    public static final DeduplicationPolicy DEFAULT =
        new DeduplicationPolicy(Distance.ofKilometers(2), SimilarityThreshold.DEFAULT);

    public DeduplicationPolicy {
        if (venueRadius == null || venueRadius.meters() <= 0) throw new IllegalArgumentException("Venue radius must be positive");
        if (venueMinSimilarity == null) throw new IllegalArgumentException("Venue similarity threshold is required");
    }

    /** Misma regla que aplica la consulta nativa; útil para dobles de prueba y comprobaciones en memoria. */
    public boolean isPossibleDuplicate(Distance distance, double similarity) {
        return distance.isWithin(venueRadius) && venueMinSimilarity.isReachedBy(similarity);
    }
}
