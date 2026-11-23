package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.ridei.sessions.domain.model.LapTimePrecision;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.PersonalRecord.ManualPoint;
import com.ridei.sessions.domain.model.PersonalRecord.SessionBest;
import com.ridei.sessions.domain.model.TimeSource;

/**
 * Récord del piloto en un trazado con una moto (ficha de trazado 14/22D, ficha de moto).
 *
 * @param bestLap    "Tu récord GPS"; {@code null} si en ese trazado con esa moto solo hay tiempos manuales
 * @param idealLap   "ideal histórica": mejores sectores de todas las sesiones con la misma
 *                   geometría; {@code null} si no hay vueltas con todos los sectores. Solo GPS
 * @param bestManual "Mejor manual ✎"; {@code null} si no hay tiempos manuales
 * @param evolution  sesiones GPS y tiempos manuales, del más antiguo al más reciente
 */
public record PersonalRecordResponseDTO(
    UUID venueId,
    String venueName,
    UUID layoutId,
    String layoutName,
    SessionDetailResponseDTO.MotorbikeDTO motorbike,
    RecordLapDTO bestLap,
    HistoricIdealDTO idealLap,
    ManualBestDTO bestManual,
    int sessionCount,
    int validLapCount,
    int manualTimeCount,
    List<EvolutionDTO> evolution
) {
    public record RecordLapDTO(UUID sessionId, int lap, long timeMillis, Instant date) {}

    public record HistoricSectorDTO(int index, long timeMillis, UUID sessionId, int lap) {}

    public record HistoricIdealDTO(long timeMillis, List<HistoricSectorDTO> sectors) {}

    public record ManualBestDTO(UUID manualTimeId, long timeMillis, LapTimePrecision precision, LocalDate date, TimeSource source) {}

    /**
     * @param source       GPS o MANUAL
     * @param sessionId    solo GPS
     * @param manualTimeId solo MANUAL
     * @param startedAt    solo GPS; los tiempos manuales solo tienen {@code date}
     * @param date         día (UTC en las sesiones GPS)
     * @param precision    THOUSANDTHS en GPS; la apuntada en los manuales
     */
    public record EvolutionDTO(
        String source,
        UUID sessionId,
        UUID manualTimeId,
        Instant startedAt,
        LocalDate date,
        long bestLapMillis,
        LapTimePrecision precision
    ) {}

    public static PersonalRecordResponseDTO fromDomain(PersonalRecord r) {
        return new PersonalRecordResponseDTO(
            r.layout().venueId().value(), r.layout().venueName(), r.layout().layoutId().value(), r.layout().layoutName(),
            SessionDetailResponseDTO.MotorbikeDTO.fromDomain(r.motorbike()),
            r.bestLap().map(b -> new RecordLapDTO(b.sessionId().value(), b.lapNumber(), b.millis(), b.date())).orElse(null),
            r.idealLap().map(i -> new HistoricIdealDTO(i.totalMillis(), i.sectors().stream()
                .map(s -> new HistoricSectorDTO(s.index(), s.millis(), s.sessionId().value(), s.lapNumber())).toList())).orElse(null),
            r.bestManual().map(m -> new ManualBestDTO(m.id().value(), m.millis(), m.precision(), m.date(), m.source())).orElse(null),
            r.sessionCount(),
            r.validLapCount(),
            r.manualTimeCount(),
            r.evolution().stream().map(PersonalRecordResponseDTO::evolution).toList());
    }

    private static EvolutionDTO evolution(PersonalRecord.EvolutionPoint point) {
        return switch (point) {
            case SessionBest s -> new EvolutionDTO(
                "GPS", s.sessionId().value(), null, s.startedAt(), s.date(), s.bestLapMillis(), LapTimePrecision.THOUSANDTHS);
            case ManualPoint m -> new EvolutionDTO(
                "MANUAL", null, m.id().value(), null, m.date(), m.millis(), m.precision());
        };
    }
}
