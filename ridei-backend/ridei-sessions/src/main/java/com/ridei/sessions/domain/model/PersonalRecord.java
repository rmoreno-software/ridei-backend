package com.ridei.sessions.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

/**
 * Récord de un piloto en un trazado con una moto: solo se comparan sesiones de la misma moto y
 * el mismo trazado. El récord GPS y el mejor tiempo manual van por separado: un tiempo apuntado a
 * mano nunca quita el récord a una sesión grabada.
 *
 * @param layout       el de la sesión o el tiempo manual más reciente
 * @param bestLap      mejor vuelta grabada con GPS; vacío si solo hay tiempos manuales
 * @param idealLap     mejores sectores de todas las sesiones con la misma geometría que la más
 *                     reciente ("ideal histórica"); vacío si ninguna vuelta tiene todos los sectores.
 *                     Solo GPS: los tiempos manuales no tienen sectores
 * @param bestManual   mejor tiempo apuntado a mano; vacío si no hay
 * @param evolution    mejor vuelta de cada sesión y cada tiempo manual, del más antiguo al más reciente
 */
public record PersonalRecord(
    LayoutRef layout,
    MotorbikeSnapshot motorbike,
    Optional<RecordLap> bestLap,
    Optional<HistoricIdealLap> idealLap,
    Optional<ManualBest> bestManual,
    int sessionCount,
    int validLapCount,
    int manualTimeCount,
    List<EvolutionPoint> evolution
) {
    public record RecordLap(SessionId sessionId, int lapNumber, long millis, Instant date) {}

    public record HistoricSector(int index, long millis, SessionId sessionId, int lapNumber) {}

    public record HistoricIdealLap(long totalMillis, List<HistoricSector> sectors) {
        public HistoricIdealLap {
            sectors = List.copyOf(sectors);
        }
    }

    public record ManualBest(ManualLapTimeId id, long millis, LapTimePrecision precision, LocalDate date, TimeSource source) {}

    /** Un punto del gráfico de evolución: una sesión GPS o un tiempo manual. */
    public sealed interface EvolutionPoint permits SessionBest, ManualPoint {
        long millis();

        /** Para ordenar: los tiempos manuales solo tienen día y cuentan desde su comienzo (UTC). */
        Instant at();

        LocalDate date();
    }

    public record SessionBest(SessionId sessionId, Instant startedAt, long bestLapMillis) implements EvolutionPoint {
        @Override public long millis() { return bestLapMillis; }
        @Override public Instant at() { return startedAt; }
        @Override public LocalDate date() { return startedAt.atOffset(ZoneOffset.UTC).toLocalDate(); }
    }

    public record ManualPoint(ManualLapTimeId id, LocalDate date, long millis, LapTimePrecision precision, TimeSource source) implements EvolutionPoint {
        @Override public Instant at() { return date.atStartOfDay(ZoneOffset.UTC).toInstant(); }
    }

    public PersonalRecord {
        if (bestLap.isEmpty() && bestManual.isEmpty()) throw new IllegalArgumentException("A record needs a GPS lap or a manual time");
        evolution = List.copyOf(evolution);
    }
}
