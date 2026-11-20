package com.ridei.circuits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class LayoutPathTest {

    /** Cuadrado de ~111 m de lado recorrido en sentido horario (N → E → S → O). */
    private static final List<GeoPoint> CLOCKWISE_SQUARE = List.of(
        GeoPoint.of(41.000, 2.000), GeoPoint.of(41.001, 2.000),
        GeoPoint.of(41.001, 2.001332), GeoPoint.of(41.000, 2.001332));

    @Nested
    class Validation {

        @Test
        void needsAtLeastThreePoints() {
            assertThatThrownBy(() -> LayoutPath.of(CLOCKWISE_SQUARE.subList(0, 2)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("at least 3");
        }

        @Test
        void aRepeatedClosingPointDoesNotCountAsAPoint() {
            List<GeoPoint> closedTriangle = List.of(CLOCKWISE_SQUARE.get(0), CLOCKWISE_SQUARE.get(1), CLOCKWISE_SQUARE.get(0));

            assertThatThrownBy(() -> LayoutPath.of(closedTriangle)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void rejectsNullPointsAndANullList() {
            List<GeoPoint> withNull = new ArrayList<>(CLOCKWISE_SQUARE);
            withNull.set(2, null);

            assertThatThrownBy(() -> LayoutPath.of(withNull)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> LayoutPath.simplified(null)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void coordinatesOutOfRangeCannotEvenBeBuilt() {
            assertThatThrownBy(() -> GeoPoint.of(90.5, 2)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> GeoPoint.of(41, -180.5)).isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> GeoPoint.of(Double.NaN, 2)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void ofRejectsMoreThanTheMaximumButSimplifiedReducesIt() {
            List<GeoPoint> circle = circle(1000);

            assertThatThrownBy(() -> LayoutPath.of(circle)).isInstanceOf(IllegalArgumentException.class);
            assertThat(LayoutPath.simplified(circle).points()).hasSizeBetween(LayoutPath.MIN_POINTS, LayoutPath.MAX_POINTS);
        }
    }

    @Nested
    class Shape {

        @Test
        void dropsTheRepeatedClosingPoint() {
            List<GeoPoint> closed = new ArrayList<>(CLOCKWISE_SQUARE);
            closed.add(CLOCKWISE_SQUARE.get(0));

            assertThat(LayoutPath.of(closed).points()).isEqualTo(CLOCKWISE_SQUARE);
        }

        @Test
        void lengthIncludesTheClosingSegment() {
            assertThat(LayoutPath.of(CLOCKWISE_SQUARE).length().meters()).isCloseTo(2 * 111.2 + 2 * 111.8, within(1.0));
        }

        @Test
        void directionFollowsThePointOrder() {
            List<GeoPoint> reversed = new ArrayList<>(CLOCKWISE_SQUARE);
            java.util.Collections.reverse(reversed);

            assertThat(LayoutPath.of(CLOCKWISE_SQUARE).direction()).isEqualTo(Direction.CLOCKWISE);
            assertThat(LayoutPath.of(reversed).direction()).isEqualTo(Direction.COUNTER_CLOCKWISE);
        }

        @Test
        void simplificationRemovesPointsOnStraightsAndKeepsCorners() {
            List<GeoPoint> withMidpoints = Arrays.asList(
                CLOCKWISE_SQUARE.get(0), GeoPoint.of(41.0005, 2.000), CLOCKWISE_SQUARE.get(1),
                GeoPoint.of(41.001, 2.000666), CLOCKWISE_SQUARE.get(2), CLOCKWISE_SQUARE.get(3));

            assertThat(LayoutPath.simplified(withMidpoints).points()).containsExactlyElementsOf(CLOCKWISE_SQUARE);
        }

        @Test
        void simplificationKeepsLengthAndDirection() {
            List<GeoPoint> circle = circle(1000);
            LayoutPath detailed = LayoutPath.simplified(circle.subList(0, 150)); // < 200: solo Douglas-Peucker
            LayoutPath simplified = LayoutPath.simplified(circle);

            assertThat(simplified.length().meters()).isCloseTo(2 * Math.PI * 500, within(2 * Math.PI * 500 * 0.01));
            assertThat(simplified.direction()).isEqualTo(Direction.COUNTER_CLOCKWISE);
            assertThat(detailed.points()).isNotEmpty();
        }
    }

    /** Círculo de 500 m de radio en sentido antihorario. */
    private static List<GeoPoint> circle(int points) {
        List<GeoPoint> result = new ArrayList<>();
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            result.add(GeoPoint.of(41 + 500 * Math.sin(angle) / 111_320, 2 + 500 * Math.cos(angle) / (111_320 * Math.cos(Math.toRadians(41)))));
        }
        return result;
    }
}
