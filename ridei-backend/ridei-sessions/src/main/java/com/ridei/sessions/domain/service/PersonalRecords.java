package com.ridei.sessions.domain.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.PersonalRecord.HistoricIdealLap;
import com.ridei.sessions.domain.model.PersonalRecord.HistoricSector;
import com.ridei.sessions.domain.model.PersonalRecord.RecordLap;
import com.ridei.sessions.domain.model.PersonalRecord.SessionBest;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.TrackGeometry;

/** Récords personales por (trazado, moto) a partir de las sesiones de un piloto. */
public final class PersonalRecords {

    private PersonalRecords() {}

    public record Key(LayoutId layoutId, MotorbikeId motorbikeId) {
        public static Key of(Session session) {
            return new Key(session.layout().layoutId(), session.motorbike().motorbikeId());
        }
    }

    /** Un récord por cada (trazado, moto) con al menos una vuelta válida. */
    public static List<PersonalRecord> of(List<Session> sessions) {
        Map<Key, List<Session>> byKey = new LinkedHashMap<>();
        sessions.stream()
            .sorted(Comparator.comparing(Session::startedAt))
            .forEach(s -> byKey.computeIfAbsent(Key.of(s), k -> new ArrayList<>()).add(s));
        List<PersonalRecord> records = new ArrayList<>();
        byKey.values().forEach(group -> record(group).ifPresent(records::add));
        return records;
    }

    /** @param chronological sesiones de un mismo (trazado, moto), de la más antigua a la más reciente */
    private static Optional<PersonalRecord> record(List<Session> chronological) {
        RecordLap best = null;
        List<SessionBest> evolution = new ArrayList<>();
        int validLaps = 0;
        for (Session session : chronological) {
            validLaps += session.timing().validLaps().size();
            Optional<Lap> lap = session.bestLap();
            if (lap.isEmpty()) continue;
            evolution.add(new SessionBest(session.id(), session.startedAt(), lap.get().durationMillis()));
            if (best == null || lap.get().durationMillis() < best.millis()) {
                best = new RecordLap(session.id(), lap.get().number(), lap.get().durationMillis(), lap.get().startedAt());
            }
        }
        if (best == null) return Optional.empty();
        Session latest = chronological.get(chronological.size() - 1);
        return Optional.of(new PersonalRecord(
            latest.layout(), latest.motorbike(), best, historicIdeal(chronological, latest.layout().geometry()),
            chronological.size(), validLaps, evolution));
    }

    /** Solo sesiones con la misma geometría: si cambió la meta, los sectores ya no son comparables. */
    private static Optional<HistoricIdealLap> historicIdeal(List<Session> sessions, TrackGeometry geometry) {
        int sectorCount = geometry.sectorCount();
        HistoricSector[] best = new HistoricSector[sectorCount];
        for (Session session : sessions) {
            if (!session.layout().geometry().equals(geometry)) continue;
            for (Lap lap : session.timing().validLaps()) {
                if (!lap.hasAllSectors()) continue;
                for (int k = 1; k <= sectorCount; k++) {
                    long millis = lap.sector(k).orElseThrow();
                    if (best[k - 1] == null || millis < best[k - 1].millis()) {
                        best[k - 1] = new HistoricSector(k, millis, session.id(), lap.number());
                    }
                }
            }
        }
        if (best[0] == null) return Optional.empty();
        long total = 0;
        for (HistoricSector sector : best) total += sector.millis();
        return Optional.of(new HistoricIdealLap(total, List.of(best)));
    }
}
