package com.ridei.circuits.domain.model;

import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public final class LayoutName extends CatalogName {

    private static final Pattern NUMBER = Pattern.compile("\\d+");

    private LayoutName(String value) {
        super(value, "Layout name");
    }

    public static LayoutName of(String value) {
        return new LayoutName(value);
    }

    public boolean isSimilarTo(LayoutName other) {
        return isSimilarTo(other, SimilarityThreshold.DEFAULT);
    }

    /**
     * Como en {@link CatalogName}, pero dos nombres con números distintos nunca son similares:
     * «Variante 5» y «Variante 6» (los nombres que pone la app a los trazados sin nombre) son
     * trazados distintos aunque compartan casi todos los trigramas.
     */
    public boolean isSimilarTo(LayoutName other, SimilarityThreshold threshold) {
        if (!numbers().equals(other.numbers())) return false;
        return super.isSimilarTo(other, threshold);
    }

    private List<String> numbers() {
        return NUMBER.matcher(normalized()).results().map(MatchResult::group).toList();
    }
}
