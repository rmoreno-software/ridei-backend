package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.ridei.sessions.domain.model.PersonalRecord;

/**
 * Récord del piloto en un trazado con una moto (ficha de trazado, ficha de moto).
 *
 * @param idealLap "ideal histórica": mejores sectores de todas las sesiones con la misma
 *                 geometría; {@code null} si no hay vueltas con todos los sectores
 */
public record PersonalRecordResponseDTO(
    UUID venueId,
    String venueName,
    UUID layoutId,
    String layoutName,
    SessionDetailResponseDTO.MotorbikeDTO motorbike,
    RecordLapDTO bestLap,
    HistoricIdealDTO idealLap,
    int sessionCount,
    int validLapCount,
    List<EvolutionDTO> evolution
) {
    public record RecordLapDTO(UUID sessionId, int lap, long timeMillis, Instant date) {}

    public record HistoricSectorDTO(int index, long timeMillis, UUID sessionId, int lap) {}

    public record HistoricIdealDTO(long timeMillis, List<HistoricSectorDTO> sectors) {}

    public record EvolutionDTO(UUID sessionId, Instant startedAt, long bestLapMillis) {}

    public static PersonalRecordResponseDTO fromDomain(PersonalRecord r) {
        return new PersonalRecordResponseDTO(
            r.layout().venueId().value(), r.layout().venueName(), r.layout().layoutId().value(), r.layout().layoutName(),
            SessionDetailResponseDTO.MotorbikeDTO.fromDomain(r.motorbike()),
            new RecordLapDTO(r.bestLap().sessionId().value(), r.bestLap().lapNumber(), r.bestLap().millis(), r.bestLap().date()),
            r.idealLap().map(i -> new HistoricIdealDTO(i.totalMillis(), i.sectors().stream()
                .map(s -> new HistoricSectorDTO(s.index(), s.millis(), s.sessionId().value(), s.lapNumber())).toList())).orElse(null),
            r.sessionCount(),
            r.validLapCount(),
            r.evolution().stream().map(e -> new EvolutionDTO(e.sessionId().value(), e.startedAt(), e.bestLapMillis())).toList());
    }
}
