package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.ManualLapEntry;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.TimeSource;
import com.ridei.sessions.domain.model.TrackCondition;

/**
 * Tiempo manual ("✎ MANUAL"). Tiempos en milisegundos; {@code precision} dice cuántos decimales
 * mostrar (SECONDS 1:52 · TENTHS 1:49.8 · HUNDREDTHS 1:14.22 · THOUSANDTHS 1:45.300).
 */
public record ManualLapTimeResponseDTO(
    UUID id,
    RefDTO venue,
    String trackName,
    RefDTO layout,
    SessionDetailResponseDTO.MotorbikeDTO motorbike,
    LocalDate date,
    long bestLapMillis,
    LapTimePrecision precision,
    TimeSource source,
    Integer lapCount,
    TrackCondition trackCondition,
    String tyres,
    List<Long> otherLapsMillis,
    String notes,
    Instant createdAt
) {
    public record RefDTO(UUID id, String name) {}

    public static ManualLapTimeResponseDTO fromDomain(ManualLapTime t) {
        ManualLapEntry e = t.entry();
        return new ManualLapTimeResponseDTO(
            t.id().value(),
            new RefDTO(t.layout().venueId().value(), t.layout().venueName()),
            t.layout().trackName(),
            new RefDTO(t.layout().layoutId().value(), t.layout().layoutName()),
            SessionDetailResponseDTO.MotorbikeDTO.fromDomain(t.motorbike()),
            e.date(), e.bestLapMillis(), e.precision(), e.source(), e.lapCount(), e.trackCondition(), e.tyres(),
            e.otherLapsMillis(), e.notes(), t.createdAt());
    }
}
