package com.ridei.circuits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CatalogNameTest {

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "Circuit de Barcelona-Catalunya | barcelona catalunya",
        "circuito barcelona catalunya   | barcelona catalunya",
        "Circuit d'Alcarràs             | alcarras",
        "Circuito de Jerez – Ángel Nieto | jerez angel nieto",
        "  MotorLand   Aragón           | motorland aragon",
        "Parcmotor Castellolí           | parcmotor castelloli",
        "Pista de Velocidad             | velocidad"
    })
    void normalizesLowercaseWithoutDiacriticsPunctuationAndGenericWords(String raw, String expected) {
        assertThat(VenueName.of(raw).normalized()).isEqualTo(expected);
    }

    @Test
    void keepsTheOriginalNameTrimmed() {
        VenueName name = VenueName.of("  Circuit   de Barcelona-Catalunya ");

        assertThat(name.value()).isEqualTo("Circuit de Barcelona-Catalunya");
    }

    @Test
    void fallsBackToAllWordsWhenTheNameIsOnlyGenericWords() {
        assertThat(TrackName.of("Circuito").normalized()).isEqualTo("circuito");
    }

    @Test
    void barcelonaCatalunyaSpellingsAreSimilar() {
        VenueName official = VenueName.of("Circuit de Barcelona-Catalunya");
        VenueName typed = VenueName.of("circuito barcelona catalunya");

        assertThat(official.isSimilarTo(typed)).isTrue();
        assertThat(official.similarityTo(typed)).isEqualTo(1.0);
    }

    @Test
    void totallyDifferentNamesAreNotSimilar() {
        assertThat(VenueName.of("Circuito de Navarra").isSimilarTo(VenueName.of("Kartódromo Los Arcos"))).isFalse();
    }

    @Test
    void layoutVariantsWithExtraWordsAreNotSimilar() {
        assertThat(LayoutName.of("GP").isSimilarTo(LayoutName.of("GP con chicane"))).isFalse();
    }

    @Test
    void rejectsBlankOrSymbolOnlyNames() {
        assertThatThrownBy(() -> VenueName.of("  ")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> VenueName.of("--- !!")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNamesLongerThanTheLimit() {
        assertThatThrownBy(() -> VenueName.of("x".repeat(CatalogName.MAX_LENGTH + 1)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
