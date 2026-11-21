package com.ridei.sessions.domain.model;

/**
 * Una muestra del logger (el móvil): posición GPS y, si los hay, velocidad, precisión e
 * inclinación (IMU).
 *
 * @param timeMillis     instante del fix GPS (epoch ms), no el reloj del sistema
 * @param speedMps       velocidad GPS (m/s); {@code null} = se deriva de las posiciones
 * @param accuracyMeters precisión horizontal estimada; {@code null} = desconocida
 * @param leanDegrees    inclinación (+ derecha, − izquierda); {@code null} = sin IMU
 */
public record TelemetrySample(
    long timeMillis,
    GeoPoint position,
    Double speedMps,
    Double accuracyMeters,
    Double leanDegrees
) {
    public static final double MAX_SPEED_MPS = 120;

    /**
     * Desde lo que envía el logger, tolerante con lo que los móviles marcan como "no válido":
     * iOS da {@code speed = -1} y precisión negativa sin un fix válido (típico parado en el box
     * al empezar a grabar). Un campo opcional inválido se ignora; un fix inválido se descarta
     * (vacío), en vez de rechazar toda la sesión.
     */
    public static java.util.Optional<TelemetrySample> fromLogger(
        Long timeMillis, Double latitude, Double longitude, Double speedMps, Double accuracyMeters, Double leanDegrees
    ) {
        if (timeMillis == null || timeMillis <= 0 || latitude == null || longitude == null) return java.util.Optional.empty();
        if (!Double.isFinite(latitude) || !Double.isFinite(longitude) || Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
            return java.util.Optional.empty();
        }
        if (latitude == 0 && longitude == 0) return java.util.Optional.empty();
        if (accuracyMeters != null && (!Double.isFinite(accuracyMeters) || accuracyMeters < 0)) return java.util.Optional.empty();
        Double speed = speedMps != null && Double.isFinite(speedMps) && speedMps >= 0 && speedMps <= MAX_SPEED_MPS ? speedMps : null;
        Double lean = leanDegrees != null && Double.isFinite(leanDegrees) && Math.abs(leanDegrees) <= 90 ? leanDegrees : null;
        return java.util.Optional.of(new TelemetrySample(timeMillis, GeoPoint.of(latitude, longitude), speed, accuracyMeters, lean));
    }

    public TelemetrySample {
        if (timeMillis <= 0) throw new IllegalArgumentException("Sample time must be a positive epoch in milliseconds");
        if (position == null) throw new IllegalArgumentException("Sample position is required");
        if (speedMps != null && (!Double.isFinite(speedMps) || speedMps < 0 || speedMps > MAX_SPEED_MPS)) {
            throw new IllegalArgumentException("Sample speed must be between 0 and 120 m/s");
        }
        if (accuracyMeters != null && (!Double.isFinite(accuracyMeters) || accuracyMeters < 0)) {
            throw new IllegalArgumentException("Sample accuracy cannot be negative");
        }
        if (leanDegrees != null && (!Double.isFinite(leanDegrees) || Math.abs(leanDegrees) > 90)) {
            throw new IllegalArgumentException("Sample lean must be between -90 and 90 degrees");
        }
    }
}
