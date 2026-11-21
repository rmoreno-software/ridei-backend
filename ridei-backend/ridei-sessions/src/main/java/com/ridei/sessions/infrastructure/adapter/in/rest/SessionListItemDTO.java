package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.util.UUID;

import com.ridei.sessions.application.SessionSummary;
import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.Session;

/** Tarjeta del historial de sesiones. Tiempos en milisegundos. */
public record SessionListItemDTO(
    UUID id,
    String name,
    Instant startedAt,
    UUID venueId,
    String venueName,
    UUID layoutId,
    String layoutName,
    SessionDetailResponseDTO.MotorbikeDTO motorbike,
    ConditionsDTO conditions,
    Long bestLapMillis,
    Long idealLapMillis,
    int lapCount,
    int validLapCount,
    boolean personalBest
) {
    public static SessionListItemDTO fromSummary(SessionSummary summary) {
        Session s = summary.session();
        return new SessionListItemDTO(
            s.id().value(), s.name(), s.startedAt(),
            s.layout().venueId().value(), s.layout().venueName(), s.layout().layoutId().value(), s.layout().layoutName(),
            SessionDetailResponseDTO.MotorbikeDTO.fromDomain(s.motorbike()),
            ConditionsDTO.fromDomain(s.conditions()),
            s.bestLap().map(Lap::durationMillis).orElse(null),
            s.idealLap().map(IdealLap::totalMillis).orElse(null),
            s.timing().laps().size(),
            s.timing().validLaps().size(),
            summary.personalBest());
    }
}
