package com.ridei.circuits.domain.model;

public final class TrackName extends CatalogName {

    private TrackName(String value) {
        super(value, "Track name");
    }

    public static TrackName of(String value) {
        return new TrackName(value);
    }

    public boolean isSimilarTo(TrackName other) {
        return isSimilarTo(other, SimilarityThreshold.DEFAULT);
    }

    public boolean isSimilarTo(TrackName other, SimilarityThreshold threshold) {
        return super.isSimilarTo(other, threshold);
    }
}
