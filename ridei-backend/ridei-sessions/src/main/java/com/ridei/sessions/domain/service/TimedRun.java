package com.ridei.sessions.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.model.GeoPoint;
import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapTiming;
import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.LapTrace.TracePoint;
import com.ridei.sessions.domain.model.PlanarProjection;
import com.ridei.sessions.domain.model.PlanarProjection.Vector;
import com.ridei.sessions.domain.model.TelemetrySample;
import com.ridei.sessions.domain.service.LapTimer.Event;
import com.ridei.sessions.domain.service.LapTimer.Track;

/**
 * Una tanda ya cronometrada: las vueltas y, bajo demanda, la traza de cada una indexada por
 * distancia (y la de la vuelta ideal cosida con los mejores sectores).
 */
public final class TimedRun {

    private static final double MPS_TO_KMH = 3.6;

    private final Track track;
    private final LapTiming timing;
    private final List<Event[]> boundaries;

    TimedRun(Track track, LapTiming timing, List<Event[]> boundaries) {
        this.track = track;
        this.timing = timing;
        this.boundaries = List.copyOf(boundaries);
    }

    public LapTiming timing() {
        return timing;
    }

    public Optional<LapTrace> trace(int lapNumber) {
        if (lapNumber < 1 || lapNumber > boundaries.size()) return Optional.empty();
        return Optional.of(buildTrace(lapNumber, boundaries.get(lapNumber - 1)));
    }

    /**
     * La vuelta ideal como traza: cada sector sale de la vuelta en la que se hizo su mejor tiempo,
     * desplazado en el tiempo para empezar donde acaba el sector anterior de la ideal.
     */
    public Optional<LapTrace> idealTrace() {
        Optional<IdealLap> ideal = timing.idealLap();
        if (ideal.isEmpty()) return Optional.empty();
        int sectorCount = timing.sectorCount();
        List<TracePoint> points = new ArrayList<>();
        double startOfSector = 0;
        for (int k = 1; k <= sectorCount; k++) {
            IdealLap.IdealSector sector = ideal.get().sector(k);
            LapTrace source = trace(sector.fromLap()).orElseThrow();
            double from = sectorStart(k);
            double to = sectorEnd(k);
            double sourceStart = source.elapsedAt(from);
            double offset = startOfSector;
            source.points().stream()
                .filter(p -> p.distanceMeters() >= from && p.distanceMeters() <= to)
                .filter(p -> points.isEmpty() || p.distanceMeters() > points.get(points.size() - 1).distanceMeters())
                .forEach(p -> points.add(new TracePoint(
                    p.distanceMeters(),
                    Math.round(offset + p.elapsedMillis() - sourceStart),
                    p.speedKmh(), p.position(), p.leanDegrees())));
            startOfSector += sector.millis();
        }
        return Optional.of(new LapTrace(0, ideal.get().totalMillis(), track.length, points));
    }

    private double sectorStart(int k) {
        return k == 1 ? 0 : track.geometry.sectorEndMeters(k - 1);
    }

    private double sectorEnd(int k) {
        return track.geometry.sectorEndMeters(k);
    }

    // ── Traza de una vuelta ─────────────────────────────────────────────────────────────

    private record RawPoint(double timeMillis, double traveledMeters, Vector plane, GeoPoint position, double speedKmh, Double lean) {}

    private LapTrace buildTrace(int lapNumber, Event[] lapBoundaries) {
        Event start = lapBoundaries[0];
        Event end = lapBoundaries[lapBoundaries.length - 1];
        PlanarProjection projection = track.projection();

        List<RawPoint> raw = new ArrayList<>();
        for (Event boundary : lapBoundaries) {
            if (boundary != null) raw.add(interpolated(boundary, projection));
        }
        for (int i = start.segment() + 1; i <= end.segment(); i++) {
            TelemetrySample sample = track.samples.get(i);
            raw.add(new RawPoint(sample.timeMillis(), track.traveled[i], track.plane[i], sample.position(), speedKmh(i), sample.leanDegrees()));
        }
        raw.sort(Comparator.comparingDouble(RawPoint::timeMillis));

        DistanceNormalizer normalizer = normalizer(lapBoundaries);
        Lap lap = timing.lap(lapNumber).orElseThrow();
        List<TracePoint> points = new ArrayList<>(raw.size());
        double lastDistance = 0;
        for (RawPoint p : raw) {
            double distance = Math.max(lastDistance, normalizer.toTrackDistance(p.traveledMeters() - start.traveledMeters()));
            lastDistance = distance;
            points.add(new TracePoint(
                round(distance, 1),
                Math.round(p.timeMillis() - start.timeMillis()),
                round(p.speedKmh(), 1),
                p.position(),
                p.lean()));
        }
        return new LapTrace(lapNumber, lap.durationMillis(), track.length, points);
    }

    private RawPoint interpolated(Event event, PlanarProjection projection) {
        int s = event.segment();
        double f = (event.traveledMeters() - track.traveled[s]) / Math.max(1e-9, track.traveled[s + 1] - track.traveled[s]);
        f = Math.max(0, Math.min(1, f));
        Vector plane = track.plane[s].plus(track.plane[s + 1].minus(track.plane[s]).times(f));
        Double leanA = track.samples.get(s).leanDegrees();
        Double leanB = track.samples.get(s + 1).leanDegrees();
        Double lean = leanA != null && leanB != null ? leanA + f * (leanB - leanA) : null;
        double speed = speedKmh(s) + f * (speedKmh(s + 1) - speedKmh(s));
        return new RawPoint(event.timeMillis(), event.traveledMeters(), plane, projection.toGeo(plane), speed, lean);
    }

    /** Velocidad GPS si el logger la envía; si no, diferencia centrada de las posiciones. */
    private double speedKmh(int i) {
        Double reported = track.samples.get(i).speedMps();
        if (reported != null) return reported * MPS_TO_KMH;
        int a = Math.max(0, i - 1);
        int b = Math.min(track.samples.size() - 1, i + 1);
        long dt = track.samples.get(b).timeMillis() - track.samples.get(a).timeMillis();
        return dt <= 0 ? 0 : (track.traveled[b] - track.traveled[a]) / (dt / 1000.0) * MPS_TO_KMH;
    }

    /** Recorrido real dentro de la vuelta → distancia sobre el trazado, por tramos entre puertas. */
    private DistanceNormalizer normalizer(Event[] lapBoundaries) {
        Event start = lapBoundaries[0];
        List<double[]> anchors = new ArrayList<>();
        anchors.add(new double[] {0, 0});
        int sectorCount = lapBoundaries.length - 1;
        for (int k = 1; k <= sectorCount; k++) {
            Event boundary = lapBoundaries[k];
            if (boundary == null) continue;
            double traveled = boundary.traveledMeters() - start.traveledMeters();
            if (traveled > anchors.get(anchors.size() - 1)[0]) anchors.add(new double[] {traveled, sectorEnd(k)});
        }
        return new DistanceNormalizer(anchors);
    }

    private record DistanceNormalizer(List<double[]> anchors) {
        double toTrackDistance(double traveled) {
            for (int i = 1; i < anchors.size(); i++) {
                double[] a = anchors.get(i - 1);
                double[] b = anchors.get(i);
                if (traveled <= b[0]) return a[1] + (traveled - a[0]) / (b[0] - a[0]) * (b[1] - a[1]);
            }
            return anchors.get(anchors.size() - 1)[1];
        }
    }

    private static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }
}
