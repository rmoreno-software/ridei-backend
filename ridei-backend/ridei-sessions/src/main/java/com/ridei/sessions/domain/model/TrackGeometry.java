package com.ridei.sessions.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ridei.sessions.domain.model.PlanarProjection.Vector;

/**
 * Lo que hace falta de un trazado para cronometrarlo: la meta y el dibujo, congelados en la
 * sesión en el momento de subirla (el catálogo puede cambiar después; la sesión no).
 *
 * <p>El dibujo es un anillo abierto en orden de recorrido que <b>empieza en la meta</b> (así lo
 * entrega {@code ridei-circuits} cuando el trazado tiene meta). Los sectores son fracciones
 * iguales de su longitud: el catálogo aún no define sectores y así los límites son estables entre
 * sesiones, que es lo que necesitan los récords y la vuelta ideal histórica.
 */
public final class TrackGeometry {

    public static final int DEFAULT_SECTOR_COUNT = 4;
    public static final int MIN_SECTOR_COUNT = 1;
    public static final int MAX_SECTOR_COUNT = 10;
    /** Media anchura de las puertas de sector (no hay muro de boxes que respetar). */
    public static final double SECTOR_GATE_HALF_WIDTH_METERS = 20;

    private final Gate finishLine;
    private final List<GeoPoint> path;
    private final int sectorCount;
    private final double lengthMeters;

    private TrackGeometry(Gate finishLine, List<GeoPoint> path, int sectorCount) {
        this.finishLine = finishLine;
        this.path = path;
        this.sectorCount = sectorCount;
        this.lengthMeters = ringLength(path);
    }

    public static TrackGeometry of(Gate finishLine, List<GeoPoint> path, int sectorCount) {
        if (finishLine == null) throw new IllegalArgumentException("Finish line is required");
        if (path == null || path.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Layout path is required");
        }
        List<GeoPoint> open = new ArrayList<>(path);
        if (open.size() > 1 && open.get(0).equals(open.get(open.size() - 1))) open.remove(open.size() - 1);
        if (open.size() < 3) throw new IllegalArgumentException("Layout path needs at least 3 points");
        if (sectorCount < MIN_SECTOR_COUNT || sectorCount > MAX_SECTOR_COUNT) {
            throw new IllegalArgumentException("Sector count must be between " + MIN_SECTOR_COUNT + " and " + MAX_SECTOR_COUNT);
        }
        return new TrackGeometry(finishLine, List.copyOf(open), sectorCount);
    }

    public static TrackGeometry of(Gate finishLine, List<GeoPoint> path) {
        return of(finishLine, path, DEFAULT_SECTOR_COUNT);
    }

    public Gate finishLine() {
        return finishLine;
    }

    public List<GeoPoint> path() {
        return path;
    }

    public int sectorCount() {
        return sectorCount;
    }

    /** Longitud del dibujo (haversine con cierre). */
    public double lengthMeters() {
        return lengthMeters;
    }

    /** El mismo trazado recorrido al revés: sigue empezando en la meta y la puerta se invierte. */
    public TrackGeometry reversed() {
        List<GeoPoint> reversed = new ArrayList<>(path.size());
        reversed.add(path.get(0));
        for (int i = path.size() - 1; i >= 1; i--) reversed.add(path.get(i));
        return new TrackGeometry(finishLine.reversed(), List.copyOf(reversed), sectorCount);
    }

    /** Distancia desde la meta, por el dibujo, a la que acaba el sector {@code index} (1..n). */
    public double sectorEndMeters(int index) {
        if (index < 1 || index > sectorCount) throw new IllegalArgumentException("No sector " + index);
        return lengthMeters * index / sectorCount;
    }

    /**
     * Puerta perpendicular al dibujo al final del sector {@code index} (1..n-1); el final del
     * último sector es la meta.
     */
    public Gate sectorGate(int index) {
        if (index < 1 || index >= sectorCount) throw new IllegalArgumentException("Sector " + index + " has no inner gate");
        double target = sectorEndMeters(index);
        PlanarProjection projection = PlanarProjection.around(path.get(0));
        double walked = 0;
        for (int i = 0; i < path.size(); i++) {
            GeoPoint from = path.get(i);
            GeoPoint to = path.get((i + 1) % path.size());
            double segment = from.distanceMetersTo(to);
            if (walked + segment >= target && segment > 0) {
                Vector a = projection.toPlane(from);
                Vector b = projection.toPlane(to);
                Vector travel = b.minus(a);
                Vector center = a.plus(travel.times((target - walked) / segment));
                Vector left = travel.unit().leftNormal().times(SECTOR_GATE_HALF_WIDTH_METERS);
                return Gate.of(projection.toGeo(center.plus(left)), projection.toGeo(center.minus(left)));
            }
            walked += segment;
        }
        throw new IllegalStateException("Sector boundary beyond the layout path");
    }

    private static double ringLength(List<GeoPoint> points) {
        double meters = 0;
        for (int i = 0; i < points.size(); i++) {
            meters += points.get(i).distanceMetersTo(points.get((i + 1) % points.size()));
        }
        return meters;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TrackGeometry that
            && sectorCount == that.sectorCount && finishLine.equals(that.finishLine) && path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(finishLine, path, sectorCount);
    }
}
