package com.ridei.circuits.domain.model;

/**
 * Línea de meta de un trazado: puerta (segmento) que cruza la pista, la que usa un crono GPS
 * para detectar el paso por meta. Mirando en el sentido de la marcha, {@code left} es el
 * extremo de la izquierda y {@code right} el de la derecha; así la app sabe en qué sentido se
 * cruza (un paso en sentido contrario no es una vuelta).
 */
public record FinishLine(GeoPoint left, GeoPoint right) {

    /** Más ancha que cualquier pista, pero sin llegar a abarcar tramos paralelos lejanos. */
    public static final double MAX_WIDTH_METERS = 100;
    public static final double MIN_WIDTH_METERS = 1;

    public FinishLine {
        if (left == null || right == null) throw new IllegalArgumentException("Finish line needs two points");
        left = left.roundedToMicrodegrees();
        right = right.roundedToMicrodegrees();
        double width = left.distanceTo(right).meters();
        if (width < MIN_WIDTH_METERS || width > MAX_WIDTH_METERS) {
            throw new IllegalArgumentException(
                "Finish line must be between " + (int) MIN_WIDTH_METERS + " and " + (int) MAX_WIDTH_METERS + " meters wide");
        }
    }

    public static FinishLine of(GeoPoint left, GeoPoint right) {
        return new FinishLine(left, right);
    }

    public Distance width() {
        return left.distanceTo(right);
    }
}
