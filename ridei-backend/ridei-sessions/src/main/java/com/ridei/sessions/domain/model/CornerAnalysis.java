package com.ridei.sessions.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Análisis por curva de una sesión: ficha de cada curva (vuelta a vuelta) y ranking de dónde se
 * pierde tiempo. Las curvas se detectan en la mejor vuelta por sus mínimos de velocidad; cada
 * curva es una zona [punto de frenada, punto de frenada de la siguiente) que cubre la vuelta
 * entera, así la suma de pérdidas es lo recuperable por curvas.
 *
 * @param referenceLap la mejor vuelta, sobre la que se detectan las curvas
 */
public record CornerAnalysis(int referenceLap, List<Corner> corners) {

    public CornerAnalysis {
        corners = List.copyOf(corners);
    }

    /**
     * @param brakingMetersBeforeApex distancia del punto de frenada (máxima velocidad antes de la
     *                                curva) al vértice de referencia
     * @param maxLeanDegrees          {@code null} sin datos de IMU
     */
    public record CornerPass(
        int lap,
        long timeMillis,
        double entrySpeedKmh,
        double minSpeedKmh,
        double exitSpeedKmh,
        double brakingMetersBeforeApex,
        Double maxLeanDegrees
    ) {}

    /**
     * @param bestLapLossMillis lo que la mejor vuelta pierde en esta curva frente al mejor paso
     * @param averageLossMillis pérdida media de las vueltas válidas frente al mejor paso
     * @param cause             causa principal de la pérdida de la mejor vuelta
     */
    public record Corner(
        int number,
        TurnDirection direction,
        double apexDistanceMeters,
        GeoPoint apex,
        double zoneStartMeters,
        double zoneEndMeters,
        List<CornerPass> passes,
        int bestPassLap,
        long bestLapLossMillis,
        long averageLossMillis,
        CornerLossCause cause
    ) {
        public Corner {
            passes = List.copyOf(passes);
        }

        public Optional<CornerPass> pass(int lap) {
            return passes.stream().filter(p -> p.lap() == lap).findFirst();
        }
    }

    public Optional<Corner> corner(int number) {
        return corners.stream().filter(c -> c.number() == number).findFirst();
    }

    /** Curvas de más a menos tiempo perdido por la mejor vuelta. */
    public List<Corner> rankedByLoss() {
        return corners.stream()
            .sorted(Comparator.comparingLong(Corner::bestLapLossMillis).reversed().thenComparingInt(Corner::number))
            .toList();
    }

    public long totalRecoverableMillis() {
        return corners.stream().mapToLong(Corner::bestLapLossMillis).sum();
    }
}
