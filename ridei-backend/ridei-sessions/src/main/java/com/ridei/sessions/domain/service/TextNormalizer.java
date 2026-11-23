package com.ridei.sessions.domain.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Minúsculas sin tildes ni signos, para comparar lo que escribe el piloto con el catálogo. */
final class TextNormalizer {
    private TextNormalizer() {}

    static String normalize(String text) {
        if (text == null) return "";
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return decomposed.toLowerCase(Locale.ROOT).replaceAll("[^\\p{Alnum}]+", " ").strip();
    }

    static List<String> words(String text) {
        String normalized = normalize(text);
        return normalized.isEmpty() ? List.of() : Arrays.asList(normalized.split(" "));
    }
}
