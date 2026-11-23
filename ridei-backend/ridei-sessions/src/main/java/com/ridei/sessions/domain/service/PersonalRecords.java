package com.ridei.sessions.domain.service;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutRef;
import com.ridei.sessions.domain.model.ManualLapTime;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.PersonalRecord;
import com.ridei.sessions.domain.model.PersonalRecord.EvolutionPoint;
import com.ridei.sessions.domain.model.PersonalRecord.HistoricIdealLap;
import com.ridei.sessions.domain.model.PersonalRecord.HistoricSector;
import com.ridei.sessions.domain.model.PersonalRecord.ManualBest;
import com.ridei.sessions.domain.model.PersonalRecord.ManualPoint;
import com.ridei.sessions.domain.model.PersonalRecord.RecordLap;
import com.ridei.sessions.domain.model.PersonalRecord.SessionBest;
import com.ridei.sessions.domain.model.Session;
import com.ridei.sessions.domain.model.SessionId;
import com.ridei.sessions.domain.model.TrackGeometry;

/** Récords personales por (trazado, moto) a partir de las sesiones y los tiempos manuales de un piloto. */
public final class PersonalRecords {

    private PersonalRecords() {}

    public record Key(LayoutId layoutId, MotorbikeId motorbikeId) {
        public static Key of(Session session) {
            return new Key(session.layout().layoutId(), session.motorbike().motorbikeId());
        }

        public static Key of(ManualLapTime time) {
            return new Key(time.layout().layoutId(), time.motorbike().motorbikeId());
        }
    }

    private static final class Group {
        final List<Session> sessions = new ArrayList<>();
        final List<ManualLapTime> manualTimes = new ArrayList<>();
    }

    /** Un récord por cada (trazado, moto) con al menos una vuelta GPS válida o un tiempo manual. */
    public static List<PersonalRecord> of(List<Session> sessions, List<ManualLapTime> manualTimes) {
        Map<Key, Group> byKey = new LinkedHashMap<>();
        sessions.stream()
            .sorted(Comparator.comparing(Session::startedAt))
            .forEach(s -> byKey.computeIfAbsent(Key.of(s), k -> new Group()).sessions.add(s));
        manualTimes.stream()
            .sorted(Comparator.comparing(ManualLapTime::date).thenComparing(ManualLapTime::createdAt))
            .forEach(t -> byKey.computeIfAbsent(Key.of(t), k -> new Group()).manualTimes.add(t));
        List<PersonalRecord> records = new ArrayList<>();
        byKey.values().forEach(group -> record(group).ifPresent(records::add));
        return records;
    }

    /**
     * Qué sesión tiene el récord GPS de cada (trazado, moto): la primera que bajó el mejor tiempo.
     * Los tiempos manuales no cuentan.
     */
    public static Map<Key, SessionId> gpsRecordHolders(List<Session> sessions) {
        Map<Key, SessionId> holders = new HashMap<>();
        Map<Key, Long> best = new HashMap<>();
        sessions.stream().sorted(Comparator.comparing(Session::startedAt)).forEach(s -> s.bestLap().ifPresent(lap -> {
            Key key = Key.of(s);
            Long current = best.get(key);
            if (current == null || lap.durationMillis() < current) {
                best.put(key, lap.durationMillis());
                holders.put(key, s.id());
            }
        }));
        return holders;
    }

    private static Optional<PersonalRecord> record(Group group) {
        List<Session> chronological = group.sessions;
        RecordLap best = null;
        List<EvolutionPoint> evolution = new ArrayList<>();
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

        ManualBest bestManual = null;
        for (ManualLapTime time : group.manualTimes) {
            evolution.add(new ManualPoint(time.id(), time.date(), time.bestLapMillis(), time.entry().precision(), time.entry().source()));
            if (bestManual == null || time.bestLapMillis() < bestManual.millis()) {
                bestManual = new ManualBest(time.id(), time.bestLapMillis(), time.entry().precision(), time.date(), time.entry().source());
            }
        }
        if (best == null && bestManual == null) return Optional.empty();
        evolution.sort(Comparator.comparing(EvolutionPoint::at));

        Session latestSession = chronological.isEmpty() ? null : chronological.get(chronological.size() - 1);
        ManualLapTime latestManual = group.manualTimes.isEmpty() ? null : group.manualTimes.get(group.manualTimes.size() - 1);
        boolean sessionIsLatest = latestSession != null
            && (latestManual == null || !latestSession.startedAt().isBefore(latestManual.date().atStartOfDay(ZoneOffset.UTC).toInstant()));
        LayoutRef layout = sessionIsLatest ? latestSession.layout().ref() : latestManual.layout();
        MotorbikeSnapshot motorbike = sessionIsLatest ? latestSession.motorbike() : latestManual.motorbike();

        Optional<HistoricIdealLap> ideal = latestSession == null
            ? Optional.empty()
            : historicIdeal(chronological, latestSession.layout().geometry());
        return Optional.of(new PersonalRecord(
            layout, motorbike, Optional.ofNullable(best), ideal, Optional.ofNullable(bestManual),
            chronological.size(), validLaps, group.manualTimes.size(), evolution));
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
