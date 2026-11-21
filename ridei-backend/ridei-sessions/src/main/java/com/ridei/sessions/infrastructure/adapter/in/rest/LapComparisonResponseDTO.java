package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import com.ridei.sessions.domain.model.LapComparison;

/**
 * Gráfico velocidad–distancia de una vuelta frente a una referencia, con el delta acumulado.
 *
 * @param referenceLap {@code 0} si la referencia es la vuelta ideal
 * @param points       d = distancia (m), v / refV = km/h, delta = ms acumulados (positivo = la
 *                     vuelta va por detrás de la referencia)
 */
public record LapComparisonResponseDTO(
    int lap,
    long lapTimeMillis,
    int referenceLap,
    long referenceTimeMillis,
    long finalDeltaMillis,
    int lengthMeters,
    List<PointDTO> points
) {
    public record PointDTO(double d, double v, double refV, long delta) {}

    public static LapComparisonResponseDTO fromDomain(LapComparison comparison) {
        return new LapComparisonResponseDTO(
            comparison.lap().lapNumber(),
            comparison.lap().durationMillis(),
            comparison.reference().lapNumber(),
            comparison.reference().durationMillis(),
            comparison.finalDeltaMillis(),
            (int) Math.round(comparison.lap().lengthMeters()),
            comparison.points().stream()
                .map(p -> new PointDTO(p.distanceMeters(), p.speedKmh(), p.referenceSpeedKmh(), p.deltaMillis()))
                .toList());
    }
}
