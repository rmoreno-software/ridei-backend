package com.ridei.circuits.domain.model;

import static com.ridei.circuits.domain.model.CatalogFixtures.NOW;
import static com.ridei.circuits.domain.model.CatalogFixtures.layout;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class LayoutTest {

    private final Layout gp = Layout.create(layout("GP", 4657, 14, Direction.CLOCKWISE), NOW);

    @Test
    void isEquivalentWhenTheNameIsSimilar() {
        assertThat(gp.isEquivalentTo(layout("Circuito GP", 3000, 8, Direction.COUNTER_CLOCKWISE))).isTrue();
    }

    @Test
    void isEquivalentWithinTheLengthToleranceAndSameTurnsAndDirection() {
        int limit = 4657 + Layout.EQUIVALENT_LENGTH_TOLERANCE_METERS;

        assertThat(gp.isEquivalentTo(layout("Variante F1", limit, 14, Direction.CLOCKWISE))).isTrue();
    }

    @Test
    void isNotEquivalentJustOutsideTheLengthTolerance() {
        int outside = 4657 - Layout.EQUIVALENT_LENGTH_TOLERANCE_METERS - 1;

        assertThat(gp.isEquivalentTo(layout("Variante F1", outside, 14, Direction.CLOCKWISE))).isFalse();
    }

    @Test
    void isNotEquivalentWithDifferentTurns() {
        assertThat(gp.isEquivalentTo(layout("GP con chicane", 4675, 16, Direction.CLOCKWISE))).isFalse();
    }

    @Test
    void isNotEquivalentWithOppositeDirection() {
        assertThat(gp.isEquivalentTo(layout("Inverso", 4657, 14, Direction.COUNTER_CLOCKWISE))).isFalse();
    }

    @Test
    void unknownTurnsOnOnlyOneSideAreNotTheSameTurns() {
        assertThat(gp.isEquivalentTo(layout("Sin datos", 4657, null, Direction.CLOCKWISE))).isFalse();
    }

    @Test
    void newLayoutsArePendingReviewAndNotDefault() {
        assertThat(gp.getStatus()).isEqualTo(CatalogStatus.PENDING_REVIEW);
        assertThat(gp.isDefault()).isFalse();
    }
}
