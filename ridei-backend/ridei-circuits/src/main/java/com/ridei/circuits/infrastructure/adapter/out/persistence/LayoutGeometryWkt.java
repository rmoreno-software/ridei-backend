package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.ridei.circuits.domain.model.FinishLine;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.LayoutPath;

/**
 * Único punto de conversión entre la geometría del dominio y las columnas PostGIS de
 * {@code layout} ({@code geometry(LineString, 4326)}, leídas y escritas como WKT, que usa el
 * orden {@code longitud latitud}):
 * <ul>
 *   <li>{@code path}: anillo <b>cerrado</b> (el último punto repite el primero); el dominio lo
 *       tiene abierto.</li>
 *   <li>{@code finish_line}: dos puntos, izquierda → derecha según el sentido de marcha.</li>
 * </ul>
 */
final class LayoutGeometryWkt {

    private static final String PREFIX = "LINESTRING(";

    private LayoutGeometryWkt() {
    }

    static String pathToWkt(LayoutPath path) {
        if (path == null) return null;
        List<GeoPoint> closed = new ArrayList<>(path.points());
        closed.add(closed.get(0));
        return toWkt(closed);
    }

    static LayoutPath pathFromWkt(String wkt) {
        return wkt == null ? null : LayoutPath.of(points(wkt)); // descarta el punto de cierre
    }

    static String finishLineToWkt(FinishLine finishLine) {
        return finishLine == null ? null : toWkt(List.of(finishLine.left(), finishLine.right()));
    }

    static FinishLine finishLineFromWkt(String wkt) {
        if (wkt == null) return null;
        List<GeoPoint> points = points(wkt);
        if (points.size() != 2) throw new IllegalStateException("Finish line must have two points: " + wkt);
        return FinishLine.of(points.get(0), points.get(1));
    }

    private static String toWkt(List<GeoPoint> points) {
        return points.stream()
            .map(point -> String.format(Locale.ROOT, "%.6f %.6f", point.longitude(), point.latitude()))
            .collect(Collectors.joining(",", PREFIX, ")"));
    }

    private static List<GeoPoint> points(String wkt) {
        String text = wkt.trim();
        if (!text.toUpperCase(Locale.ROOT).startsWith(PREFIX) || !text.endsWith(")")) {
            throw new IllegalStateException("Unexpected layout geometry: " + text);
        }
        List<GeoPoint> points = new ArrayList<>();
        for (String pair : text.substring(PREFIX.length(), text.length() - 1).split(",")) {
            String[] lonLat = pair.trim().split("\s+");
            points.add(GeoPoint.of(Double.parseDouble(lonLat[1]), Double.parseDouble(lonLat[0])));
        }
        return points;
    }
}
