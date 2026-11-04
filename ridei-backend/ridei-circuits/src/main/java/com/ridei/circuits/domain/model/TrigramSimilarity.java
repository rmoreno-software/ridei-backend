package com.ridei.circuits.domain.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Réplica en Java de {@code similarity()} de la extensión {@code pg_trgm}: cada palabra
 * alfanumérica se rodea de dos espacios delante y uno detrás, se extrae el conjunto de
 * trigramas y la similitud es |A ∩ B| / |A ∪ B|. Mantenerla idéntica garantiza que el
 * dominio y las consultas nativas toman la misma decisión de duplicado.
 */
public final class TrigramSimilarity {

    private TrigramSimilarity() {
    }

    public static double between(String a, String b) {
        Set<String> left = trigrams(a);
        Set<String> right = trigrams(b);
        if (left.isEmpty() || right.isEmpty()) return 0.0;

        Set<String> common = new HashSet<>(left);
        common.retainAll(right);
        int union = left.size() + right.size() - common.size();
        return (double) common.size() / union;
    }

    static Set<String> trigrams(String text) {
        Set<String> trigrams = new HashSet<>();
        for (String word : text.toLowerCase().split("[^\\p{L}\\p{N}]+")) {
            if (word.isEmpty()) continue;
            String padded = "  " + word + " ";
            for (int i = 0; i + 3 <= padded.length(); i++) {
                trigrams.add(padded.substring(i, i + 3));
            }
        }
        return trigrams;
    }
}
