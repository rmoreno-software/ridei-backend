package com.ridei.sessions.domain.service;

import static com.ridei.sessions.domain.CalafatFixture.GEOMETRY;
import static com.ridei.sessions.domain.CalafatFixture.ride;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

import com.ridei.sessions.domain.CalafatFixture;
import com.ridei.sessions.domain.model.CornerAnalysis;
import com.ridei.sessions.domain.model.CornerAnalysis.Corner;
import com.ridei.sessions.domain.model.CornerLossCause;
import com.ridei.sessions.domain.model.Telemetry;

class CornerAnalyzerTest {

    /** Una "curva" (mínimo de velocidad) cada 400 m: ~8 en los 3,24 km de Calafat. */
    static final DoubleUnaryOperator PROFILE = s -> 26 + 14 * Math.cos(2 * Math.PI * s / 400);

    @Test
    void detectsEveryCornerOfTheBestLap() {
        TimedRun run = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 10, 1, false, 1)), GEOMETRY);
        CornerAnalysis analysis = CornerAnalyzer.analyze(run).orElseThrow();

        assertThat(analysis.corners()).hasSize(8);
        for (int k = 0; k < 8; k++) {
            assertThat(analysis.corners().get(k).apexDistanceMeters()).isCloseTo(200 + 400 * k, within(20.0));
            assertThat(analysis.corners().get(k).pass(analysis.referenceLap()).orElseThrow().minSpeedKmh()).isCloseTo(12 * 3.6, within(3.0));
        }
    }

    @Test
    void zonesCoverTheWholeLap() {
        TimedRun run = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 5, 1, false, 2)), GEOMETRY);
        CornerAnalysis analysis = CornerAnalyzer.analyze(run).orElseThrow();

        assertThat(analysis.corners().get(0).zoneStartMeters()).isZero();
        assertThat(analysis.corners().get(analysis.corners().size() - 1).zoneEndMeters()).isCloseTo(GEOMETRY.lengthMeters(), within(0.1));
        for (int k = 1; k < analysis.corners().size(); k++) {
            assertThat(analysis.corners().get(k).zoneStartMeters()).isEqualTo(analysis.corners().get(k - 1).zoneEndMeters());
        }
    }

    @Test
    void findsWhereTheBestLapLosesTime() {
        // la vuelta 2 es la mejor (3 % más rápida) salvo entre 800 y 1200 m, donde va un 10 % más lenta
        CalafatFixture.LapProfile profile = (lap, d) -> PROFILE.applyAsDouble(d)
            * (lap == 2 ? (d >= 800 && d <= 1200 ? 0.90 : 1.03) : 1.0);
        TimedRun run = LapTimer.time(Telemetry.recorded(ride(2, profile, 10, 0, false, 3)), GEOMETRY);
        CornerAnalysis analysis = CornerAnalyzer.analyze(run).orElseThrow();

        assertThat(analysis.referenceLap()).isEqualTo(2);
        Corner worst = analysis.rankedByLoss().get(0);
        assertThat(worst.apexDistanceMeters()).isCloseTo(1000, within(60.0));
        assertThat(worst.bestPassLap()).isEqualTo(1);
        assertThat(worst.bestLapLossMillis()).isPositive();
        assertThat(worst.cause()).isEqualTo(CornerLossCause.MIN_SPEED);
        assertThat(analysis.corners()).filteredOn(c -> c != worst).allMatch(c -> c.cause() == CornerLossCause.NONE || c.bestLapLossMillis() < 50);
        assertThat(analysis.totalRecoverableMillis()).isEqualTo(analysis.corners().stream().mapToLong(Corner::bestLapLossMillis).sum());
    }
}
