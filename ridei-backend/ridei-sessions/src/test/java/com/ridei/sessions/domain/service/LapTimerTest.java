package com.ridei.sessions.domain.service;

import static com.ridei.sessions.domain.CalafatFixture.GEOMETRY;
import static com.ridei.sessions.domain.CalafatFixture.exactLapSeconds;
import static com.ridei.sessions.domain.CalafatFixture.ride;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.junit.jupiter.api.Test;

import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapTiming;
import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.LapValidity;
import com.ridei.sessions.domain.model.PlanarProjection;
import com.ridei.sessions.domain.model.PlanarProjection.Vector;
import com.ridei.sessions.domain.model.Telemetry;
import com.ridei.sessions.domain.model.TelemetrySample;

/** Sobre la geometría real de Calafat (meta de 16,5 m hasta el muro de boxes), no un cuadrado de juguete. */
class LapTimerTest {

    /** Rectas a ~40 m/s y curvas a ~12 m/s, cada 400 m. */
    static final DoubleUnaryOperator PROFILE = s -> 26 + 14 * Math.cos(2 * Math.PI * s / 400);

    @Test
    void timesEveryLapAt1HzWithPhoneGpsNoise() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(5, PROFILE, 1, 3, false, 1)), GEOMETRY).timing();

        double expected = exactLapSeconds(PROFILE, false);
        assertThat(timing.reversedDirection()).isFalse();
        assertThat(timing.laps()).hasSize(5).allMatch(Lap::isValid);
        timing.laps().forEach(lap -> assertThat(lap.durationMillis() / 1000.0).isCloseTo(expected, within(0.25)));
    }

    @Test
    void lapTimesAreAccurateWithTenHzGps() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 10, 1, false, 2)), GEOMETRY).timing();

        double expected = exactLapSeconds(PROFILE, false);
        assertThat(timing.laps()).hasSize(3);
        timing.laps().forEach(lap -> assertThat(lap.durationMillis() / 1000.0).isCloseTo(expected, within(0.05)));
    }

    @Test
    void highRateNoisyGpsDoesNotInflateTheLapDistance() {
        // a 10 Hz, sumar distancias entre fixes con ±3 m de ruido daba recorridos > 115 % del trazado
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 10, 3, false, 9)), GEOMETRY).timing();

        assertThat(timing.laps()).hasSize(3).allMatch(Lap::isValid);
        timing.laps().forEach(lap -> assertThat(lap.distanceMeters() / GEOMETRY.lengthMeters()).isCloseTo(1.0, within(0.03)));
    }

    @Test
    void theIdealLapIsNeverSlowerThanTheBestLap() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(4, PROFILE, 1, 3, false, 10)), GEOMETRY).timing();

        assertThat(timing.idealLap().orElseThrow().totalMillis())
            .isLessThanOrEqualTo(timing.bestLap().orElseThrow().durationMillis());
    }

    @Test
    void invalidMarkersFromPhonesDoNotSpoilTheSession() {
        // iOS: speed = -1 y precisión negativa sin fix válido (parado en el box al empezar a grabar)
        List<TelemetrySample> clean = ride(3, PROFILE, 1, 2, false, 11);
        List<TelemetrySample> raw = new ArrayList<>();
        long t0 = clean.get(0).timeMillis();
        for (int i = 30; i >= 1; i--) {
            TelemetrySample first = clean.get(0);
            TelemetrySample.fromLogger(t0 - i * 1000L, first.position().latitude(), first.position().longitude(), -1.0, -1.0, 120.0)
                .ifPresent(raw::add);
            TelemetrySample.fromLogger(t0 - i * 1000L + 500, first.position().latitude(), first.position().longitude(), -1.0, 4.0, 120.0)
                .ifPresent(raw::add);
        }
        TelemetrySample.fromLogger(t0 - 100, 0.0, 0.0, 0.0, 5.0, null).ifPresent(raw::add);
        raw.addAll(clean);

        assertThat(raw).hasSize(30 + clean.size());
        assertThat(raw.get(0).speedMps()).isNull();
        assertThat(raw.get(0).leanDegrees()).isNull();
        LapTiming withMarkers = LapTimer.time(Telemetry.recorded(raw), GEOMETRY).timing();
        LapTiming without = LapTimer.time(Telemetry.recorded(clean), GEOMETRY).timing();
        assertThat(withMarkers.laps()).isEqualTo(without.laps());
    }

    @Test
    void outLapAndInLapAreNotLaps() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(2, PROFILE, 5, 0, false, 3)), GEOMETRY).timing();

        assertThat(timing.laps()).extracting(Lap::number).containsExactly(1, 2);
    }

    @Test
    void sectorsAddUpToTheLapTime() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 1, 2, false, 4)), GEOMETRY).timing();

        assertThat(timing.sectorCount()).isEqualTo(4);
        for (Lap lap : timing.laps()) {
            assertThat(lap.hasAllSectors()).isTrue();
            long sum = lap.sectorMillis().stream().mapToLong(Long::longValue).sum();
            assertThat(sum).isEqualTo(lap.durationMillis());
        }
    }

    @Test
    void aSessionRiddenTheOtherWayRoundStillHasLaps() {
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 1, 3, true, 5)), GEOMETRY).timing();

        double expected = exactLapSeconds(PROFILE, true);
        assertThat(timing.reversedDirection()).isTrue();
        assertThat(timing.laps()).hasSize(3).allMatch(Lap::isValid);
        timing.laps().forEach(lap -> assertThat(lap.durationMillis() / 1000.0).isCloseTo(expected, within(0.25)));
    }

    @Test
    void aGpsDropoutAcrossTheLineStillTimesTheLapButFlagsIt() {
        List<TelemetrySample> samples = new ArrayList<>(ride(4, PROFILE, 1, 0, false, 6));
        long crossing = secondFinishCrossingMillis(samples);
        samples.removeIf(s -> Math.abs(s.timeMillis() - crossing) < 3_000);

        LapTiming timing = LapTimer.time(Telemetry.recorded(samples), GEOMETRY).timing();

        assertThat(timing.laps()).hasSize(4);
        assertThat(timing.laps()).filteredOn(l -> l.validity() == LapValidity.GPS_GAP).hasSize(2);
    }

    @Test
    void passingOutsideTheGateMergesTwoLapsIntoOneFlaggedTooLong() {
        List<TelemetrySample> samples = ride(4, PROFILE, 1, 0, false, 6);
        long crossing = secondFinishCrossingMillis(samples);
        // 40 m a la izquierda de la pista alrededor de la meta: más allá de la puerta alargada
        PlanarProjection projection = PlanarProjection.around(GEOMETRY.path().get(0));
        Vector left = projection.toPlane(GEOMETRY.path().get(1)).minus(projection.toPlane(GEOMETRY.path().get(0)))
            .unit().leftNormal().times(40);
        List<TelemetrySample> outside = samples.stream()
            .map(s -> Math.abs(s.timeMillis() - crossing) > 4_000 ? s
                : new TelemetrySample(s.timeMillis(), projection.toGeo(projection.toPlane(s.position()).plus(left)),
                    s.speedMps(), s.accuracyMeters(), null))
            .toList();

        LapTiming timing = LapTimer.time(Telemetry.recorded(outside), GEOMETRY).timing();

        assertThat(timing.laps()).hasSize(3);
        assertThat(timing.laps()).filteredOn(l -> l.validity() == LapValidity.TOO_LONG).hasSize(1);
        assertThat(timing.validLaps()).hasSize(2);
    }

    /** Instante del 2.º paso por meta (fin de la vuelta 1) en una tanda sin ruido. */
    private static long secondFinishCrossingMillis(List<TelemetrySample> samples) {
        Lap first = LapTimer.time(Telemetry.recorded(samples), GEOMETRY).timing().laps().get(0);
        return first.endedAt().toEpochMilli();
    }

    @Test
    void idealLapIsTheSumOfTheBestSectors() {
        // vuelta 2 más rápida en la primera mitad, vuelta 3 en la segunda
        LapTiming timing = LapTimer.time(Telemetry.recorded(ride(3, PROFILE, 2, 1, false, 7)), GEOMETRY).timing();
        IdealLap ideal = timing.idealLap().orElseThrow();

        long sum = ideal.sectors().stream().mapToLong(IdealLap.IdealSector::millis).sum();
        assertThat(ideal.totalMillis()).isEqualTo(sum);
        assertThat(ideal.totalMillis()).isLessThanOrEqualTo(timing.bestLap().orElseThrow().durationMillis());
    }

    @Test
    void tracesAreIndexedByTrackDistanceFromZeroToTheLayoutLength() {
        TimedRun run = LapTimer.time(Telemetry.recorded(ride(2, PROFILE, 1, 2, false, 8)), GEOMETRY);
        LapTrace trace = run.trace(1).orElseThrow();

        assertThat(trace.points().get(0).distanceMeters()).isZero();
        assertThat(trace.points().get(trace.points().size() - 1).distanceMeters()).isCloseTo(GEOMETRY.lengthMeters(), within(0.1));
        assertThat(trace.points().get(trace.points().size() - 1).elapsedMillis()).isEqualTo(trace.durationMillis());
        assertThat(trace.points()).isSortedAccordingTo((a, b) -> Double.compare(a.distanceMeters(), b.distanceMeters()));

        LapTrace ideal = run.idealTrace().orElseThrow();
        assertThat(ideal.points().get(ideal.points().size() - 1).elapsedMillis())
            .isCloseTo(run.timing().idealLap().orElseThrow().totalMillis(), within(2L));
    }
}
