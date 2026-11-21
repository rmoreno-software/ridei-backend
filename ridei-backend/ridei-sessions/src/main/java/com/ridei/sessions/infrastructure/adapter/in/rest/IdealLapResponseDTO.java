package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.List;

import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.Session;

/**
 * Vuelta ideal cosida: de qué vuelta sale cada sector y cuánto mejora ese sector al de la mejor
 * vuelta real.
 */
public record IdealLapResponseDTO(
    long timeMillis,
    int bestLapNumber,
    long bestLapMillis,
    long gainMillis,
    List<SectorDTO> sectors
) {
    /** @param gainVsBestLapMillis lo que este sector de la ideal mejora al de la mejor vuelta (≥ 0) */
    public record SectorDTO(int index, double fromMeters, double toMeters, long timeMillis, int fromLap, long gainVsBestLapMillis) {}

    public static IdealLapResponseDTO from(Session session, IdealLap ideal, Lap best) {
        List<SectorDTO> sectors = ideal.sectors().stream()
            .map(s -> new SectorDTO(
                s.index(),
                Math.round(s.index() == 1 ? 0 : session.layout().geometry().sectorEndMeters(s.index() - 1)),
                Math.round(session.layout().geometry().sectorEndMeters(s.index())),
                s.millis(),
                s.fromLap(),
                best.sector(s.index()).map(b -> b - s.millis()).orElse(0L)))
            .toList();
        return new IdealLapResponseDTO(ideal.totalMillis(), best.number(), best.durationMillis(), best.durationMillis() - ideal.totalMillis(), sectors);
    }
}
