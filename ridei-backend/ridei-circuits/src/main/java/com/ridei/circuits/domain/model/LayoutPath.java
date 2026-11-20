package com.ridei.circuits.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * Dibujo de un trazado: anillo cerrado de puntos WGS84 en el orden en que se recorre la vuelta.
 * El primer punto <b>no</b> se repite al final; el cierre (último → primero) es implícito.
 *
 * <p>Entre {@value #MIN_POINTS} y {@value #MAX_POINTS} puntos: {@link #simplified(List)} reduce
 * un dibujo detallado (OSM, GPS) con Douglas-Peucker para no inflar las respuestas de listado.
 */
public final class LayoutPath {

    public static final int MIN_POINTS = 3;
    public static final int MAX_POINTS = 200;
    /** Tolerancia inicial de la simplificación; se aumenta solo si quedan más de {@link #MAX_POINTS}. */
    public static final double SIMPLIFY_TOLERANCE_METERS = 2.5;

    private static final double METERS_PER_DEGREE = 111_320;

    private final List<GeoPoint> points;

    private LayoutPath(List<GeoPoint> points) {
        this.points = points;
    }

    /**
     * Dibujo ya simplificado (p. ej. leído de la base de datos). Si el último punto repite el
     * primero se descarta.
     *
     * @throws IllegalArgumentException si hay puntos nulos, menos de 3 o más de 200 distintos
     */
    public static LayoutPath of(List<GeoPoint> points) {
        List<GeoPoint> open = open(points);
        if (open.size() > MAX_POINTS) {
            throw new IllegalArgumentException("A layout path cannot have more than " + MAX_POINTS + " points");
        }
        return new LayoutPath(open);
    }

    /**
     * Simplifica con Douglas-Peucker ({@value #SIMPLIFY_TOLERANCE_METERS} m, aumentando la
     * tolerancia hasta que quedan como mucho {@value #MAX_POINTS} puntos).
     *
     * @throws IllegalArgumentException si hay puntos nulos o menos de 3 distintos
     */
    public static LayoutPath simplified(List<GeoPoint> points) {
        List<GeoPoint> open = open(points);
        double tolerance = SIMPLIFY_TOLERANCE_METERS;
        List<GeoPoint> result = simplifyRing(open, tolerance);
        while (result.size() > MAX_POINTS) {
            tolerance *= 1.25;
            result = simplifyRing(open, tolerance);
        }
        if (result.size() < MIN_POINTS) {
            throw new IllegalArgumentException("A layout path must enclose an area (at least " + MIN_POINTS + " distinct points)");
        }
        return new LayoutPath(result);
    }

    private static List<GeoPoint> open(List<GeoPoint> points) {
        if (points == null) throw new IllegalArgumentException("Layout path points are required");
        if (points.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("A layout path cannot contain null points");
        }
        List<GeoPoint> open = new ArrayList<>(points);
        if (open.size() > 1 && open.get(0).equals(open.get(open.size() - 1))) open.remove(open.size() - 1);
        if (open.size() < MIN_POINTS) {
            throw new IllegalArgumentException("A layout path needs at least " + MIN_POINTS + " points");
        }
        return List.copyOf(open);
    }

    /** Puntos en orden de recorrido, sin repetir el primero al final. */
    public List<GeoPoint> points() {
        return points;
    }

    /** Longitud de la vuelta (haversine), incluido el cierre del último punto al primero. */
    public Distance length() {
        double meters = 0;
        for (int i = 0; i < points.size(); i++) {
            meters += points.get(i).distanceTo(points.get((i + 1) % points.size())).meters();
        }
        return Distance.ofMeters(meters);
    }

    /**
     * Sentido de recorrido según el área con signo (fórmula del lazo). En trazados que se
     * cruzan a sí mismos (en forma de 8) el resultado es el del lazo de mayor área.
     */
    public Direction direction() {
        double[][] xy = project(points);
        double area = 0;
        for (int i = 0; i < xy.length; i++) {
            double[] a = xy[i];
            double[] b = xy[(i + 1) % xy.length];
            area += a[0] * b[1] - b[0] * a[1];
        }
        return area > 0 ? Direction.COUNTER_CLOCKWISE : Direction.CLOCKWISE;
    }

    // ── Douglas-Peucker sobre un anillo ────────────────────────────────────────────────────

    /**
     * En un anillo el primer y el último punto coinciden y Douglas-Peucker degenera; se parte en
     * dos mitades por el punto más alejado del primero y se simplifica cada una.
     */
    private static List<GeoPoint> simplifyRing(List<GeoPoint> ring, double toleranceMeters) {
        double[][] xy = project(ring);
        int n = xy.length;
        int far = 0;
        double farDistance = -1;
        for (int i = 1; i < n; i++) {
            double d = Math.hypot(xy[i][0] - xy[0][0], xy[i][1] - xy[0][1]);
            if (d > farDistance) {
                farDistance = d;
                far = i;
            }
        }
        TreeSet<Integer> keep = new TreeSet<>(List.of(0, far));
        simplifySection(xy, 0, far, toleranceMeters, keep);
        simplifySection(xy, far, n, toleranceMeters, keep); // índice n = vuelta al punto 0
        keep.remove(n);
        return keep.stream().map(ring::get).toList();
    }

    private static void simplifySection(double[][] xy, int from, int to, double tolerance, TreeSet<Integer> keep) {
        if (to <= from + 1) return;
        double[] a = xy[from % xy.length];
        double[] b = xy[to % xy.length];
        double maxDistance = -1;
        int index = -1;
        for (int i = from + 1; i < to; i++) {
            double d = distanceToSegment(xy[i], a, b);
            if (d > maxDistance) {
                maxDistance = d;
                index = i;
            }
        }
        if (maxDistance > tolerance) {
            keep.add(index);
            simplifySection(xy, from, index, tolerance, keep);
            simplifySection(xy, index, to, tolerance, keep);
        }
    }

    private static double distanceToSegment(double[] p, double[] a, double[] b) {
        double dx = b[0] - a[0];
        double dy = b[1] - a[1];
        double lengthSquared = dx * dx + dy * dy;
        double t = lengthSquared == 0 ? 0 : Math.max(0, Math.min(1, ((p[0] - a[0]) * dx + (p[1] - a[1]) * dy) / lengthSquared));
        return Math.hypot(a[0] + t * dx - p[0], a[1] + t * dy - p[1]);
    }

    /** Proyección equirectangular local en metros (x = este, y = norte): suficiente a escala de un circuito. */
    private static double[][] project(List<GeoPoint> points) {
        double cosLat = Math.cos(Math.toRadians(points.get(0).latitude()));
        double[][] xy = new double[points.size()][];
        for (int i = 0; i < points.size(); i++) {
            GeoPoint p = points.get(i);
            xy[i] = new double[] {p.longitude() * METERS_PER_DEGREE * cosLat, p.latitude() * METERS_PER_DEGREE};
        }
        return xy;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof LayoutPath that && points.equals(that.points);
    }

    @Override
    public int hashCode() {
        return points.hashCode();
    }

    @Override
    public String toString() {
        return "LayoutPath[" + points.size() + " points]";
    }
}
