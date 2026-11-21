package com.ridei.sessions.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.model.CornerAnalysis;
import com.ridei.sessions.domain.model.CornerAnalysis.Corner;
import com.ridei.sessions.domain.model.CornerAnalysis.CornerPass;
import com.ridei.sessions.domain.model.CornerLossCause;
import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.LapTrace.TracePoint;
import com.ridei.sessions.domain.model.PlanarProjection;
import com.ridei.sessions.domain.model.PlanarProjection.Vector;
import com.ridei.sessions.domain.model.TurnDirection;

/**
 * Detecta las curvas en la mejor vuelta (mínimos de velocidad con frenada y aceleración claras) y
 * mide cada paso por ellas en todas las vueltas válidas.
 *
 * <p>Con un GPS de móvil a 1 Hz hay un punto cada ~40 m: las velocidades mínimas y los puntos de
 * frenada son aproximados (±1 muestra). Con loggers de 10–25 Hz la ficha es mucho más fina.
 */
public final class CornerAnalyzer {

    /** Bajada mínima de velocidad, desde la recta anterior, para considerar una curva. */
    static final double MIN_BRAKING_DROP_KMH = 15;
    /** Subida mínima después del vértice. */
    static final double MIN_EXIT_GAIN_KMH = 10;
    static final double APPROACH_WINDOW_METERS = 300;
    static final double APEX_WINDOW_METERS = 80;
    static final double MIN_DISTANCE_BETWEEN_APEXES_METERS = 80;
    static final double EXIT_OFFSET_METERS = 80;
    static final double SMOOTHING_HALF_WINDOW_METERS = 15;
    static final double BRAKING_CAUSE_THRESHOLD_METERS = 10;
    static final double SPEED_CAUSE_THRESHOLD_KMH = 3;

    private CornerAnalyzer() {}

    /** Vacío si la sesión no tiene ninguna vuelta válida. */
    public static Optional<CornerAnalysis> analyze(TimedRun run) {
        Optional<Lap> bestLap = run.timing().bestLap();
        if (bestLap.isEmpty()) return Optional.empty();
        LapTrace reference = run.trace(bestLap.get().number()).orElseThrow();
        List<LapTrace> validLaps = run.timing().validLaps().stream()
            .map(lap -> run.trace(lap.number()).orElseThrow())
            .toList();

        List<Integer> apexes = apexes(reference);
        List<Corner> corners = new ArrayList<>(apexes.size());
        double[] smoothed = smoothedSpeeds(reference);
        List<Double> brakingPoints = new ArrayList<>();
        int previousApex = 0;
        for (int apex : apexes) {
            brakingPoints.add(reference.points().get(argMax(smoothed, previousApex, apex)).distanceMeters());
            previousApex = apex;
        }

        for (int c = 0; c < apexes.size(); c++) {
            TracePoint apex = reference.points().get(apexes.get(c));
            double zoneStart = c == 0 ? 0 : brakingPoints.get(c);
            double zoneEnd = c == apexes.size() - 1 ? reference.lengthMeters() : brakingPoints.get(c + 1);
            corners.add(corner(c + 1, apex, zoneStart, zoneEnd, reference, validLaps, bestLap.get().number()));
        }
        return Optional.of(new CornerAnalysis(bestLap.get().number(), corners));
    }

    private static Corner corner(
        int number, TracePoint apex, double zoneStart, double zoneEnd,
        LapTrace reference, List<LapTrace> laps, int bestLapNumber
    ) {
        double apexDistance = apex.distanceMeters();
        List<CornerPass> passes = laps.stream()
            .map(lap -> pass(lap, apexDistance, zoneStart, zoneEnd))
            .sorted(Comparator.comparingInt(CornerPass::lap))
            .toList();
        CornerPass bestPass = passes.stream()
            .min(Comparator.comparingLong(CornerPass::timeMillis).thenComparingInt(CornerPass::lap))
            .orElseThrow();
        CornerPass bestLapPass = passes.stream().filter(p -> p.lap() == bestLapNumber).findFirst().orElseThrow();
        long bestLapLoss = bestLapPass.timeMillis() - bestPass.timeMillis();
        long averageLoss = Math.round(passes.stream().mapToLong(p -> p.timeMillis() - bestPass.timeMillis()).average().orElse(0));
        return new Corner(
            number,
            direction(reference, apexDistance),
            apexDistance,
            apex.position(),
            round(zoneStart),
            round(zoneEnd),
            passes,
            bestPass.lap(),
            bestLapLoss,
            averageLoss,
            cause(bestLapPass, bestPass, bestLapLoss));
    }

    private static CornerPass pass(LapTrace lap, double apexDistance, double zoneStart, double zoneEnd) {
        long time = Math.round(lap.elapsedAt(zoneEnd) - lap.elapsedAt(zoneStart));

        TracePoint slowest = null;
        for (TracePoint p : lap.points()) {
            if (Math.abs(p.distanceMeters() - apexDistance) > APEX_WINDOW_METERS) continue;
            if (slowest == null || p.speedKmh() < slowest.speedKmh()) slowest = p;
        }
        double lapApex = slowest != null ? slowest.distanceMeters() : apexDistance;
        double minSpeed = slowest != null ? slowest.speedKmh() : lap.speedAt(apexDistance);

        TracePoint fastestBefore = null;
        Double maxLean = null;
        for (TracePoint p : lap.points()) {
            double d = p.distanceMeters();
            if (d >= zoneStart && d <= lapApex && (fastestBefore == null || p.speedKmh() > fastestBefore.speedKmh())) {
                fastestBefore = p;
            }
            if (d >= zoneStart && d <= zoneEnd && p.leanDegrees() != null) {
                maxLean = maxLean == null ? Math.abs(p.leanDegrees()) : Math.max(maxLean, Math.abs(p.leanDegrees()));
            }
        }
        double brakingPoint = fastestBefore != null ? fastestBefore.distanceMeters() : zoneStart;
        double entrySpeed = fastestBefore != null ? fastestBefore.speedKmh() : lap.speedAt(zoneStart);
        double exitSpeed = lap.speedAt(Math.min(lapApex + EXIT_OFFSET_METERS, zoneEnd));

        return new CornerPass(
            lap.lapNumber(), time, round(entrySpeed), round(minSpeed), round(exitSpeed),
            round(Math.max(0, apexDistance - brakingPoint)), maxLean != null ? round(maxLean) : null);
    }

    private static CornerLossCause cause(CornerPass lap, CornerPass best, long loss) {
        if (loss <= 0 || lap.lap() == best.lap()) return CornerLossCause.NONE;
        if (lap.brakingMetersBeforeApex() - best.brakingMetersBeforeApex() > BRAKING_CAUSE_THRESHOLD_METERS) {
            return CornerLossCause.BRAKING;
        }
        if (best.minSpeedKmh() - lap.minSpeedKmh() > SPEED_CAUSE_THRESHOLD_KMH) return CornerLossCause.MIN_SPEED;
        if (best.exitSpeedKmh() - lap.exitSpeedKmh() > SPEED_CAUSE_THRESHOLD_KMH) return CornerLossCause.EXIT;
        return CornerLossCause.LINE;
    }

    // ── Detección de curvas en la vuelta de referencia ──────────────────────────────────

    /** Índices de los vértices: mínimos de velocidad con frenada y aceleración claras. */
    static List<Integer> apexes(LapTrace reference) {
        List<TracePoint> points = reference.points();
        double[] speed = smoothedSpeeds(reference);
        List<Integer> apexes = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            double d = points.get(i).distanceMeters();
            double before = maxSpeed(points, speed, d - APPROACH_WINDOW_METERS, d);
            double after = maxSpeed(points, speed, d, d + APPROACH_WINDOW_METERS);
            if (!isLocalMinimum(points, speed, i)) continue;
            if (before - speed[i] < MIN_BRAKING_DROP_KMH || after - speed[i] < MIN_EXIT_GAIN_KMH) continue;
            if (!apexes.isEmpty()) {
                int last = apexes.get(apexes.size() - 1);
                if (d - points.get(last).distanceMeters() < MIN_DISTANCE_BETWEEN_APEXES_METERS) {
                    if (speed[i] < speed[last]) apexes.set(apexes.size() - 1, i);
                    continue;
                }
            }
            apexes.add(i);
        }
        return apexes;
    }

    private static boolean isLocalMinimum(List<TracePoint> points, double[] speed, int i) {
        double d = points.get(i).distanceMeters();
        for (int j = 0; j < points.size(); j++) {
            if (j != i && Math.abs(points.get(j).distanceMeters() - d) <= APEX_WINDOW_METERS && speed[j] < speed[i]) return false;
        }
        return true;
    }

    private static double maxSpeed(List<TracePoint> points, double[] speed, double from, double to) {
        double max = 0;
        for (int j = 0; j < points.size(); j++) {
            double d = points.get(j).distanceMeters();
            if (d >= from && d <= to) max = Math.max(max, speed[j]);
        }
        return max;
    }

    private static double[] smoothedSpeeds(LapTrace trace) {
        List<TracePoint> points = trace.points();
        double[] smoothed = new double[points.size()];
        int from = 0;
        int to = 0;
        double sum = 0;
        for (int i = 0; i < points.size(); i++) {
            double d = points.get(i).distanceMeters();
            while (to < points.size() && points.get(to).distanceMeters() <= d + SMOOTHING_HALF_WINDOW_METERS) sum += points.get(to++).speedKmh();
            while (points.get(from).distanceMeters() < d - SMOOTHING_HALF_WINDOW_METERS) sum -= points.get(from++).speedKmh();
            smoothed[i] = sum / (to - from);
        }
        return smoothed;
    }

    private static int argMax(double[] values, int from, int to) {
        int best = from;
        for (int i = from; i <= to; i++) if (values[i] > values[best]) best = i;
        return best;
    }

    /** Sentido de giro alrededor del vértice: signo del producto vectorial entrada × salida. */
    private static TurnDirection direction(LapTrace reference, double apexDistance) {
        GeoPoint before = positionAt(reference, apexDistance - 40);
        GeoPoint apex = positionAt(reference, apexDistance);
        GeoPoint after = positionAt(reference, apexDistance + 40);
        PlanarProjection projection = PlanarProjection.around(apex);
        Vector in = projection.toPlane(apex).minus(projection.toPlane(before));
        Vector out = projection.toPlane(after).minus(projection.toPlane(apex));
        return in.cross(out) > 0 ? TurnDirection.LEFT : TurnDirection.RIGHT;
    }

    private static GeoPoint positionAt(LapTrace trace, double distance) {
        return trace.points().stream()
            .min(Comparator.comparingDouble(p -> Math.abs(p.distanceMeters() - distance)))
            .orElseThrow()
            .position();
    }

    private static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
