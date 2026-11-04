package com.ridei.circuits.domain.model;

public final class LayoutName extends CatalogName {

    private LayoutName(String value) {
        super(value, "Layout name");
    }

    public static LayoutName of(String value) {
        return new LayoutName(value);
    }

    public boolean isSimilarTo(LayoutName other) {
        return isSimilarTo(other, SimilarityThreshold.DEFAULT);
    }

    public boolean isSimilarTo(LayoutName other, SimilarityThreshold threshold) {
        return super.isSimilarTo(other, threshold);
    }
}
