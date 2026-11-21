package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import com.ridei.sessions.domain.model.LapTrace;

/**
 * Una vuelta indexada por distancia para el visor de trazada (color = velocidad) y la
 * reproducción. {@code lap = 0} es la vuelta ideal cosida.
 *
 * @param points d = distancia desde la meta (m, normalizada al trazado), t = ms desde el inicio
 *               de la vuelta, v = km/h, lean = grados (null sin IMU)
 */
public record LapTraceResponseDTO(int lap, long timeMillis, int lengthMeters, double minSpeedKmh, double maxSpeedKmh, List<TracePointDTO> points) {

    public record TracePointDTO(double d, long t, double v, double lat, double lon, Double lean) {}

    public static LapTraceResponseDTO fromDomain(LapTrace trace) {
        List<TracePointDTO> points = trace.points().stream()
            .map(p -> new TracePointDTO(p.distanceMeters(), p.elapsedMillis(), p.speedKmh(),
                p.position().latitude(), p.position().longitude(), p.leanDegrees()))
            .toList();
        double min = trace.points().stream().mapToDouble(LapTrace.TracePoint::speedKmh).min().orElse(0);
        double max = trace.points().stream().mapToDouble(LapTrace.TracePoint::speedKmh).max().orElse(0);
        return new LapTraceResponseDTO(trace.lapNumber(), trace.durationMillis(), (int) Math.round(trace.lengthMeters()), min, max, points);
    }
}
