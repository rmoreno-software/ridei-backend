package com.ridei.sessions.domain.model;

import java.util.List;

/**
 * Una vuelta frente a una referencia (otra vuelta o la ideal), punto a punto por distancia:
 * el gráfico velocidad–distancia con el delta acumulado.
 *
 * @param points para cada punto de la vuelta: su velocidad, la de la referencia a la misma
 *               distancia y el delta acumulado (positivo = la vuelta va por detrás)
 */
public record LapComparison(LapTrace lap, LapTrace reference, List<ComparisonPoint> points) {

    public record ComparisonPoint(double distanceMeters, double speedKmh, double referenceSpeedKmh, long deltaMillis) {}

    public LapComparison {
        points = List.copyOf(points);
    }

    public static LapComparison of(LapTrace lap, LapTrace reference) {
        List<ComparisonPoint> points = lap.points().stream()
            .map(p -> new ComparisonPoint(
                p.distanceMeters(),
                p.speedKmh(),
                Math.round(reference.speedAt(p.distanceMeters()) * 10) / 10.0,
                Math.round(p.elapsedMillis() - reference.elapsedAt(p.distanceMeters()))))
            .toList();
        return new LapComparison(lap, reference, points);
    }

    public long finalDeltaMillis() {
        return lap.durationMillis() - reference.durationMillis();
    }
}
