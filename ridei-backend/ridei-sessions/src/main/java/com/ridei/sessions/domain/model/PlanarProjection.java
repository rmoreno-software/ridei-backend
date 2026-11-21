package com.ridei.sessions.domain.model;

/**
 * Proyección equirectangular local en metros (x = este, y = norte) alrededor de un origen.
 * A escala de un circuito (unos km) el error es despreciable frente al del GPS.
 */
public final class PlanarProjection {

    private static final double METERS_PER_DEGREE = 111_320;

    private final GeoPoint origin;
    private final double metersPerDegreeLongitude;

    private PlanarProjection(GeoPoint origin) {
        this.origin = origin;
        this.metersPerDegreeLongitude = METERS_PER_DEGREE * Math.cos(Math.toRadians(origin.latitude()));
    }

    public static PlanarProjection around(GeoPoint origin) {
        return new PlanarProjection(origin);
    }

    public Vector toPlane(GeoPoint point) {
        return new Vector(
            (point.longitude() - origin.longitude()) * metersPerDegreeLongitude,
            (point.latitude() - origin.latitude()) * METERS_PER_DEGREE);
    }

    public GeoPoint toGeo(Vector vector) {
        return GeoPoint.of(
            origin.latitude() + vector.y() / METERS_PER_DEGREE,
            origin.longitude() + vector.x() / metersPerDegreeLongitude);
    }

    /** Vector en el plano local, en metros. */
    public record Vector(double x, double y) {

        public Vector minus(Vector other) {
            return new Vector(x - other.x, y - other.y);
        }

        public Vector plus(Vector other) {
            return new Vector(x + other.x, y + other.y);
        }

        public Vector times(double factor) {
            return new Vector(x * factor, y * factor);
        }

        public double cross(Vector other) {
            return x * other.y - y * other.x;
        }

        public double length() {
            return Math.hypot(x, y);
        }

        public Vector unit() {
            double length = length();
            return length == 0 ? this : new Vector(x / length, y / length);
        }

        /** Girado 90° en sentido antihorario: a la izquierda de la marcha si este vector es la marcha. */
        public Vector leftNormal() {
            return new Vector(-y, x);
        }
    }
}
