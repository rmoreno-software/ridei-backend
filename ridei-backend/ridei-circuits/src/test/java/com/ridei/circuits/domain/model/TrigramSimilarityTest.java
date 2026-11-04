package com.ridei.circuits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

/** Valores de referencia calculados con {@code SELECT similarity(a, b)} de pg_trgm. */
class TrigramSimilarityTest {

    @Test
    void producesPaddedWordTrigramsLikePgTrgm() {
        assertThat(TrigramSimilarity.trigrams("cat")).containsExactlyInAnyOrder("  c", " ca", "cat", "at ");
    }

    @Test
    void matchesPgTrgmForShortWords() {
        // similarity('abc', 'abd') = 0.333333 (2 trigramas comunes de 6)
        assertThat(TrigramSimilarity.between("abc", "abd")).isCloseTo(1.0 / 3, within(1e-9));
    }

    @Test
    void identicalStringsAreFullySimilar() {
        assertThat(TrigramSimilarity.between("barcelona catalunya", "barcelona catalunya")).isEqualTo(1.0);
    }

    @Test
    void wordOrderDoesNotMatter() {
        assertThat(TrigramSimilarity.between("ricardo tormo valencia", "valencia ricardo tormo")).isEqualTo(1.0);
    }

    @Test
    void emptyStringsHaveNoSimilarity() {
        assertThat(TrigramSimilarity.between("", "jerez")).isZero();
    }
}
