package com.ridei.circuits.domain.model;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalización canónica de nombres del catálogo. El resultado se persiste en
 * {@code normalized_name} y es la entrada de la similitud por trigramas, tanto en Java
 * ({@link TrigramSimilarity}) como en PostgreSQL ({@code pg_trgm}). Cambiar estas reglas
 * obliga a una migración que recalcule la columna.
 */
public final class NameNormalizer {

    /** Palabras sin valor identificativo en nombres de circuitos (es / ca / en). */
    public static final Set<String> GENERIC_WORDS = Set.of(
        "circuit", "circuits", "circuito", "circuitos", "pista", "track",
        "de", "del", "d", "la", "el", "l", "les", "els", "los", "las", "i", "y", "the", "of"
    );

    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^\\p{L}\\p{N}]+");

    private NameNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null) throw new IllegalArgumentException("Name is required");
        List<String> words = words(raw);
        List<String> meaningful = words.stream().filter(word -> !GENERIC_WORDS.contains(word)).toList();
        return String.join(" ", meaningful.isEmpty() ? words : meaningful);
    }

    private static List<String> words(String raw) {
        String withoutDiacritics = DIACRITICS.matcher(Normalizer.normalize(raw, Normalizer.Form.NFD)).replaceAll("");
        String cleaned = NON_ALPHANUMERIC.matcher(withoutDiacritics.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
        return cleaned.isEmpty() ? List.of() : Arrays.asList(cleaned.split(" "));
    }
}
