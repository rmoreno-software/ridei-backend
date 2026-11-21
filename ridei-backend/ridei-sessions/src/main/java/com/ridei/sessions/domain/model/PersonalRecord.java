package com.ridei.sessions.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Récord de un piloto en un trazado con una moto: solo se comparan sesiones de la misma moto y
 * el mismo trazado.
 *
 * @param idealLap   mejores sectores de todas las sesiones con la misma geometría que la más
 *                   reciente ("ideal histórica"); vacío si ninguna vuelta tiene todos los sectores
 * @param evolution  mejor vuelta de cada sesión, de la más antigua a la más reciente
 */
public record PersonalRecord(
    LayoutSnapshot layout,
    MotorbikeSnapshot motorbike,
    RecordLap bestLap,
    Optional<HistoricIdealLap> idealLap,
    int sessionCount,
    int validLapCount,
    List<SessionBest> evolution
) {
    public record RecordLap(SessionId sessionId, int lapNumber, long millis, Instant date) {}

    public record HistoricSector(int index, long millis, SessionId sessionId, int lapNumber) {}

    public record HistoricIdealLap(long totalMillis, List<HistoricSector> sectors) {
        public HistoricIdealLap {
            sectors = List.copyOf(sectors);
        }
    }

    public record SessionBest(SessionId sessionId, Instant startedAt, long bestLapMillis) {}

    public PersonalRecord {
        evolution = List.copyOf(evolution);
    }
}
