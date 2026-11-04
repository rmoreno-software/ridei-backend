package com.ridei.circuits.domain.model;

/**
 * Nombre de un elemento del catálogo: conserva el original para mostrarlo y expone el
 * normalizado para comparar. Base común de {@link VenueName}, {@link TrackName} y {@link LayoutName}.
 */
public abstract class CatalogName {

    public static final int MAX_LENGTH = 150;

    private final String value;
    private final String normalized;

    protected CatalogName(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " is required");
        String trimmed = value.trim().replaceAll("\\s+", " ");
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(label + " must be at most " + MAX_LENGTH + " characters");
        }
        this.normalized = NameNormalizer.normalize(trimmed);
        if (normalized.isEmpty()) throw new IllegalArgumentException(label + " must contain letters or digits");
        this.value = trimmed;
    }

    public String value() {
        return value;
    }

    public String normalized() {
        return normalized;
    }

    public double similarityTo(CatalogName other) {
        return TrigramSimilarity.between(normalized, other.normalized);
    }

    protected boolean isSimilarTo(CatalogName other, SimilarityThreshold threshold) {
        return threshold.isReachedBy(similarityTo(other));
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && value.equals(((CatalogName) other).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
