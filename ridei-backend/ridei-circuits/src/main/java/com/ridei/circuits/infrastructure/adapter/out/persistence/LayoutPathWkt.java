package com.ridei.circuits.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.LayoutPath;

/**
 * Único punto de conversión entre {@link LayoutPath} (anillo abierto) y la columna
 * {@code layout.path} ({@code geometry(LineString, 4326)}, anillo cerrado: el último punto repite
 * el primero). WKT usa el orden {@code longitud latitud}.
 */
final class LayoutPathWkt {

    private static final String PREFIX = "LINESTRING(";

    private LayoutPathWkt() {
    }

    static String toWkt(LayoutPath path) {
        if (path == null) return null;
        List<GeoPoint> closed = new ArrayList<>(path.points());
        closed.add(closed.get(0));
        return closed.stream()
            .map(point -> String.format(Locale.ROOT, "%.6f %.6f", point.longitude(), point.latitude()))
            .collect(Collectors.joining(",", PREFIX, ")"));
    }

    static LayoutPath fromWkt(String wkt) {
        if (wkt == null) return null;
        String text = wkt.trim();
        if (!text.toUpperCase(Locale.ROOT).startsWith(PREFIX) || !text.endsWith(")")) {
            throw new IllegalStateException("Unexpected layout path geometry: " + text);
        }
        List<GeoPoint> points = new ArrayList<>();
        for (String pair : text.substring(PREFIX.length(), text.length() - 1).split(",")) {
            String[] lonLat = pair.trim().split("\\s+");
            points.add(GeoPoint.of(Double.parseDouble(lonLat[1]), Double.parseDouble(lonLat[0])));
        }
        return LayoutPath.of(points); // descarta el punto de cierre
    }
}
