package com.ridei.circuits.domain.model;

public final class VenueName extends CatalogName {

    private VenueName(String value) {
        super(value, "Venue name");
    }

    public static VenueName of(String value) {
        return new VenueName(value);
    }

    public boolean isSimilarTo(VenueName other) {
        return isSimilarTo(other, SimilarityThreshold.DEFAULT);
    }

    public boolean isSimilarTo(VenueName other, SimilarityThreshold threshold) {
        return super.isSimilarTo(other, threshold);
    }
}
