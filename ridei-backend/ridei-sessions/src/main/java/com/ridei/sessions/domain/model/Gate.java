package com.ridei.sessions.domain.model;

import com.ridei.sessions.domain.model.PlanarProjection.Vector;

/**
 * Puerta que cruza la pista (meta o límite de sector). Mirando en el sentido de la marcha,
 * {@code left} es el extremo de la izquierda y {@code right} el de la derecha: así se distingue
 * un paso hacia delante de uno en sentido contrario.
 */
public record Gate(GeoPoint left, GeoPoint right) {

    public static final double MIN_WIDTH_METERS = 1;
    public static final double MAX_WIDTH_METERS = 150;

    public Gate {
        if (left == null || right == null) throw new IllegalArgumentException("A gate needs two points");
        double width = left.distanceMetersTo(right);
        if (width < MIN_WIDTH_METERS || width > MAX_WIDTH_METERS) {
            throw new IllegalArgumentException(
                "A gate must be between " + (int) MIN_WIDTH_METERS + " and " + (int) MAX_WIDTH_METERS + " meters wide");
        }
    }

    public static Gate of(GeoPoint left, GeoPoint right) {
        return new Gate(left, right);
    }

    public double widthMeters() {
        return left.distanceMetersTo(right);
    }

    /** La misma puerta para recorrer la pista en sentido contrario. */
    public Gate reversed() {
        return new Gate(right, left);
    }

    /**
     * Alarga la puerta por la izquierda. La meta oficial suele acabar en el muro de boxes (a la
     * derecha está el pit lane, que no debe contar como vuelta), pero por la izquierda un GPS de
     * móvil con varios metros de error podría pasar por fuera y fundir dos vueltas en una.
     */
    public Gate extendedToTheLeft(double meters) {
        PlanarProjection projection = PlanarProjection.around(right);
        Vector r = projection.toPlane(right);
        Vector l = projection.toPlane(left);
        Vector direction = l.minus(r).unit();
        return new Gate(projection.toGeo(l.plus(direction.times(meters))), right);
    }

    public OnPlane onPlane(PlanarProjection projection) {
        return new OnPlane(projection.toPlane(left), projection.toPlane(right));
    }

    /** La puerta proyectada, para cruzarla con tramos entre muestras GPS. */
    public record OnPlane(Vector left, Vector right) {

        /**
         * Cruce del tramo {@code from → to} con la puerta.
         *
         * @return {@code null} si no se cortan
         */
        public Crossing crossing(Vector from, Vector to) {
            Vector travel = to.minus(from);
            Vector gate = right.minus(left);
            double denominator = travel.cross(gate);
            if (denominator == 0) return null;
            Vector offset = left.minus(from);
            double t = offset.cross(gate) / denominator;
            double u = offset.cross(travel) / denominator;
            // [0, 1) en el tramo: un cruce justo en una muestra cuenta solo para el tramo que sale de ella.
            if (t < 0 || t >= 1 || u < 0 || u > 1) return null;
            return new Crossing(t, denominator < 0);
        }
    }

    /**
     * @param fraction posición del cruce dentro del tramo, de 0 (inicio) a 1 (fin)
     * @param forward  {@code true} si se cruza en el sentido de la marcha: el extremo
     *                 {@code left} de la puerta queda a la izquierda del piloto
     */
    public record Crossing(double fraction, boolean forward) {}
}
