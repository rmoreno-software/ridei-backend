package com.ridei.sessions.domain.model;

import java.util.List;

/**
 * Una vuelta indexada por distancia: lo que pinta el visor de trazada y el gráfico
 * velocidad–distancia. La distancia está normalizada a la longitud del trazado (anclada en la
 * meta y en las puertas de sector), así dos vueltas con líneas distintas se comparan punto a
 * punto.
 *
 * @param lapNumber {@code 0} para la vuelta ideal cosida
 */
public record LapTrace(int lapNumber, long durationMillis, double lengthMeters, List<TracePoint> points) {

    /**
     * @param leanDegrees {@code null} si el logger no envía inclinación
     */
    public record TracePoint(double distanceMeters, long elapsedMillis, double speedKmh, GeoPoint position, Double leanDegrees) {}

    public LapTrace {
        points = List.copyOf(points);
        if (points.size() < 2) throw new IllegalArgumentException("A lap trace needs at least 2 points");
    }

    /** Tiempo (ms desde el inicio de la vuelta) al pasar por {@code distance}, interpolado. */
    public double elapsedAt(double distance) {
        return interpolate(distance, true);
    }

    /** Velocidad (km/h) al pasar por {@code distance}, interpolada. */
    public double speedAt(double distance) {
        return interpolate(distance, false);
    }

    private double interpolate(double distance, boolean elapsed) {
        int index = indexAtOrBefore(distance);
        TracePoint a = points.get(index);
        if (index == points.size() - 1) return elapsed ? a.elapsedMillis() : a.speedKmh();
        TracePoint b = points.get(index + 1);
        double span = b.distanceMeters() - a.distanceMeters();
        double f = span <= 0 ? 0 : Math.max(0, Math.min(1, (distance - a.distanceMeters()) / span));
        return elapsed
            ? a.elapsedMillis() + f * (b.elapsedMillis() - a.elapsedMillis())
            : a.speedKmh() + f * (b.speedKmh() - a.speedKmh());
    }

    /** Búsqueda binaria: las distancias son no decrecientes. */
    private int indexAtOrBefore(double distance) {
        int low = 0;
        int high = points.size() - 1;
        if (distance <= points.get(0).distanceMeters()) return 0;
        while (low < high) {
            int mid = (low + high + 1) >>> 1;
            if (points.get(mid).distanceMeters() <= distance) low = mid;
            else high = mid - 1;
        }
        return low;
    }
}
