package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import com.ridei.sessions.domain.model.CornerAnalysis;
import com.ridei.sessions.domain.model.CornerLossCause;
import com.ridei.sessions.domain.model.TurnDirection;

/**
 * Ficha por curva y ranking de tiempo perdido. Las curvas (T1, T2…) se detectan en la mejor
 * vuelta y se numeran desde la meta.
 *
 * @param totalRecoverableMillis suma de lo que la mejor vuelta pierde en cada curva frente al
 *                               mejor paso de la sesión por ella
 * @param ranking                números de curva de más a menos tiempo perdido
 */
public record CornerAnalysisResponseDTO(int referenceLap, long totalRecoverableMillis, List<Integer> ranking, List<CornerDTO> corners) {

    /**
     * @param brakingMetersBeforeApex del punto de frenada (máx. velocidad antes de la curva) al vértice
     */
    public record PassDTO(int lap, long timeMillis, double entrySpeedKmh, double minSpeedKmh, double exitSpeedKmh,
                          double brakingMetersBeforeApex, Double maxLeanDegrees) {}

    public record CornerDTO(
        int number,
        String label,
        TurnDirection direction,
        double apexDistanceMeters,
        PointDTO apex,
        double zoneStartMeters,
        double zoneEndMeters,
        int bestPassLap,
        long bestLapLossMillis,
        long averageLossMillis,
        CornerLossCause cause,
        PassDTO bestLapPass,
        PassDTO bestPass,
        List<PassDTO> passes
    ) {}

    public static CornerAnalysisResponseDTO fromDomain(CornerAnalysis analysis) {
        List<CornerDTO> corners = analysis.corners().stream().map(c -> new CornerDTO(
            c.number(),
            "T" + c.number(),
            c.direction(),
            c.apexDistanceMeters(),
            PointDTO.fromDomain(c.apex()),
            c.zoneStartMeters(),
            c.zoneEndMeters(),
            c.bestPassLap(),
            c.bestLapLossMillis(),
            c.averageLossMillis(),
            c.cause(),
            c.pass(analysis.referenceLap()).map(CornerAnalysisResponseDTO::pass).orElse(null),
            c.pass(c.bestPassLap()).map(CornerAnalysisResponseDTO::pass).orElse(null),
            c.passes().stream().map(CornerAnalysisResponseDTO::pass).toList())).toList();
        return new CornerAnalysisResponseDTO(
            analysis.referenceLap(),
            analysis.totalRecoverableMillis(),
            analysis.rankedByLoss().stream().map(CornerAnalysis.Corner::number).toList(),
            corners);
    }

    private static PassDTO pass(CornerAnalysis.CornerPass p) {
        return new PassDTO(p.lap(), p.timeMillis(), p.entrySpeedKmh(), p.minSpeedKmh(), p.exitSpeedKmh(),
            p.brakingMetersBeforeApex(), p.maxLeanDegrees());
    }
}
