package com.ridei.sessions.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.PlanarProjection;
import com.ridei.sessions.domain.model.PlanarProjection.Vector;
import com.ridei.sessions.domain.model.TelemetrySample;
import com.ridei.sessions.domain.model.TrackGeometry;
import com.ridei.sessions.domain.model.VenueId;

/**
 * Calafat (trazado Principal) tal como lo tiene el catálogo tras la migración V8 de
 * ridei-circuits, y un simulador de tandas GPS sobre él.
 */
public final class CalafatFixture {

    public static final Gate FINISH_LINE = Gate.of(GeoPoint.of(40.932277, 0.839336), GeoPoint.of(40.932178, 0.839481));

    public static final List<GeoPoint> PATH = List.of(
        GeoPoint.of(40.932217, 0.839424),
        GeoPoint.of(40.935607, 0.843456),
        GeoPoint.of(40.936683, 0.843801),
        GeoPoint.of(40.936788, 0.843900),
        GeoPoint.of(40.936817, 0.844098),
        GeoPoint.of(40.936765, 0.844212),
        GeoPoint.of(40.936678, 0.844264),
        GeoPoint.of(40.936243, 0.844337),
        GeoPoint.of(40.935892, 0.844285),
        GeoPoint.of(40.935711, 0.844208),
        GeoPoint.of(40.935484, 0.844067),
        GeoPoint.of(40.935171, 0.843776),
        GeoPoint.of(40.933920, 0.842278),
        GeoPoint.of(40.933770, 0.842216),
        GeoPoint.of(40.933600, 0.842356),
        GeoPoint.of(40.933572, 0.842563),
        GeoPoint.of(40.933623, 0.842711),
        GeoPoint.of(40.935117, 0.844493),
        GeoPoint.of(40.935339, 0.844640),
        GeoPoint.of(40.935733, 0.844642),
        GeoPoint.of(40.935952, 0.844832),
        GeoPoint.of(40.936180, 0.845182),
        GeoPoint.of(40.936246, 0.845348),
        GeoPoint.of(40.936245, 0.845608),
        GeoPoint.of(40.936124, 0.845846),
        GeoPoint.of(40.935924, 0.845933),
        GeoPoint.of(40.935709, 0.845864),
        GeoPoint.of(40.934516, 0.844730),
        GeoPoint.of(40.933490, 0.843546),
        GeoPoint.of(40.933385, 0.843366),
        GeoPoint.of(40.933259, 0.842967),
        GeoPoint.of(40.933163, 0.842786),
        GeoPoint.of(40.933046, 0.842748),
        GeoPoint.of(40.932713, 0.842866),
        GeoPoint.of(40.932545, 0.842821),
        GeoPoint.of(40.931741, 0.842067),
        GeoPoint.of(40.931662, 0.841874),
        GeoPoint.of(40.931659, 0.841629),
        GeoPoint.of(40.931733, 0.841455),
        GeoPoint.of(40.932036, 0.841024),
        GeoPoint.of(40.932083, 0.840872),
        GeoPoint.of(40.932087, 0.840635),
        GeoPoint.of(40.931924, 0.840332),
        GeoPoint.of(40.931622, 0.840056),
        GeoPoint.of(40.931501, 0.839608),
        GeoPoint.of(40.931419, 0.839487),
        GeoPoint.of(40.930971, 0.839187),
        GeoPoint.of(40.930768, 0.839107),
        GeoPoint.of(40.929359, 0.838998),
        GeoPoint.of(40.929245, 0.838964),
        GeoPoint.of(40.929159, 0.838873),
        GeoPoint.of(40.929128, 0.838720),
        GeoPoint.of(40.929153, 0.838564),
        GeoPoint.of(40.930406, 0.836832),
        GeoPoint.of(40.930493, 0.836766),
        GeoPoint.of(40.930653, 0.836747),
        GeoPoint.of(40.930850, 0.836839),
        GeoPoint.of(40.930995, 0.837013),
        GeoPoint.of(40.931077, 0.837181),
        GeoPoint.of(40.931288, 0.837966),
        GeoPoint.of(40.931449, 0.838393),
        GeoPoint.of(40.931674, 0.838771),
        GeoPoint.of(40.932217, 0.839424)
    );

    public static final TrackGeometry GEOMETRY = TrackGeometry.of(FINISH_LINE, PATH);

    public static final LayoutSnapshot LAYOUT = new LayoutSnapshot(
        VenueId.of("10000000-0000-4000-8000-000000000009"), "Circuit de Calafat", "Velocidad",
        LayoutId.of("30000000-0000-4000-8000-000000000006"), "Principal", GEOMETRY);

    public static final MotorbikeSnapshot R6 = new MotorbikeSnapshot(
        MotorbikeId.of("40000000-0000-4000-8000-000000000001"), "Yamaha R6", "SPORT", "ASPHALT");

    public static final long START_MILLIS = 1_795_000_000_000L;

    private CalafatFixture() {}

    /**
     * Rueda {@code laps} vueltas completas más una vuelta de salida (empieza 300 m antes de la
     * meta) y una de entrada (acaba 300 m después).
     *
     * @param speedAt   velocidad (m/s) según la distancia desde la meta dentro de la vuelta
     * @param hz        frecuencia del GPS
     * @param noiseMeters error lateral máximo (uniforme, ±)
     * @param reversed  rodar en sentido contrario al del catálogo
     */
    public static List<TelemetrySample> ride(int laps, DoubleUnaryOperator speedAt, double hz, double noiseMeters, boolean reversed, long seed) {
        return ride(laps, (lap, d) -> speedAt.applyAsDouble(d), hz, noiseMeters, reversed, seed);
    }

    /** Velocidad (m/s) según la vuelta (0 = vuelta de salida, 1 = primera cronometrada…) y la distancia desde la meta. */
    @FunctionalInterface
    public interface LapProfile {
        double speed(int lap, double distance);
    }

    public static List<TelemetrySample> ride(int laps, LapProfile profile, double hz, double noiseMeters, boolean reversed, long seed) {
        TrackGeometry geometry = reversed ? GEOMETRY.reversed() : GEOMETRY;
        double length = geometry.lengthMeters();
        PlanarProjection projection = PlanarProjection.around(geometry.path().get(0));
        List<Vector> ring = geometry.path().stream().map(projection::toPlane).toList();
        Random random = new Random(seed);

        List<TelemetrySample> samples = new ArrayList<>();
        double s = -300;
        double end = laps * length + 300;
        double dt = 1.0 / hz;
        double t = 0;
        double step = 0.05;
        double nextSample = 0;
        while (s <= end) {
            if (t >= nextSample) {
                Vector p = pointAt(ring, ((s % length) + length) % length);
                Vector noise = new Vector((random.nextDouble() * 2 - 1) * noiseMeters, (random.nextDouble() * 2 - 1) * noiseMeters);
                double v = profile.speed(lapOf(s, length), ((s % length) + length) % length);
                samples.add(new TelemetrySample(START_MILLIS + Math.round(t * 1000), projection.toGeo(p.plus(noise)), v, 3.0, null));
                nextSample += dt;
            }
            s += profile.speed(lapOf(s, length), ((s % length) + length) % length) * step;
            t += step;
        }
        return samples;
    }

    /** Tiempo exacto de una vuelta con ese perfil de velocidad (integración numérica fina). */
    public static double exactLapSeconds(DoubleUnaryOperator speedAt, boolean reversed) {
        double length = (reversed ? GEOMETRY.reversed() : GEOMETRY).lengthMeters();
        double seconds = 0;
        double ds = 0.01;
        for (double s = 0; s < length; s += ds) seconds += ds / speedAt.applyAsDouble(s);
        return seconds;
    }

    private static int lapOf(double s, double length) {
        return (int) Math.floor(s / length) + 1;
    }

    private static Vector pointAt(List<Vector> ring, double distance) {
        double walked = 0;
        for (int i = 0; i < ring.size(); i++) {
            Vector a = ring.get(i);
            Vector b = ring.get((i + 1) % ring.size());
            double segment = b.minus(a).length();
            if (walked + segment >= distance) return a.plus(b.minus(a).times((distance - walked) / segment));
            walked += segment;
        }
        return ring.get(0);
    }
}
