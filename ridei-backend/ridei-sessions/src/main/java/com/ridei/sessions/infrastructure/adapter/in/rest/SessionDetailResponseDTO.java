package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ridei.sessions.application.SessionSummary;
import com.ridei.sessions.domain.model.IdealLap;
import com.ridei.sessions.domain.model.Lap;
import com.ridei.sessions.domain.model.LapValidity;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;
import com.ridei.sessions.domain.model.Session;

/** Resumen de sesión con la tabla de vueltas y sectores. Tiempos en milisegundos. */
public record SessionDetailResponseDTO(
    UUID id,
    String name,
    Instant startedAt,
    Instant endedAt,
    VenueDTO venue,
    String trackName,
    LayoutDTO layout,
    MotorbikeDTO motorbike,
    ConditionsDTO conditions,
    String notes,
    boolean personalBest,
    BestLapDTO bestLap,
    IdealSummaryDTO idealLap,
    int lapCount,
    int validLapCount,
    List<LapDTO> laps
) {
    public record VenueDTO(UUID id, String name) {}

    /**
     * @param reversedDirection la tanda se rodó al revés que el sentido del catálogo; los tiempos
     *                          valen, pero conviene revisar el sentido del trazado
     */
    public record LayoutDTO(UUID id, String name, int lengthMeters, int sectorCount, boolean reversedDirection) {}

    public record MotorbikeDTO(UUID id, String name, String category, String categoryGroup) {
        public static MotorbikeDTO fromDomain(MotorbikeSnapshot m) {
            return new MotorbikeDTO(m.motorbikeId().value(), m.displayName(), m.category(), m.categoryGroup());
        }
    }

    public record BestLapDTO(int number, long timeMillis) {}

    /** @param gainMillis lo que la ideal mejora a la mejor vuelta real */
    public record IdealSummaryDTO(long timeMillis, long gainMillis) {}

    /**
     * @param sectorsMillis         {@code null} en un sector si no se detectó su puerta
     * @param bestSectors           el sector es el mejor de la sesión (solo vueltas válidas)
     * @param deltaToBestMillis     diferencia con la mejor vuelta (solo vueltas válidas)
     */
    public record LapDTO(
        int number,
        Instant startedAt,
        long timeMillis,
        List<Long> sectorsMillis,
        List<Boolean> bestSectors,
        double distanceMeters,
        LapValidity validity,
        boolean best,
        Long deltaToBestMillis
    ) {}

    public static SessionDetailResponseDTO fromSummary(SessionSummary summary) {
        Session s = summary.session();
        Optional<Lap> best = s.bestLap();
        Optional<IdealLap> ideal = s.idealLap();
        List<Long> bestSectors = bestSectors(s);
        List<LapDTO> laps = s.timing().laps().stream().map(lap -> {
            List<Boolean> isBest = new ArrayList<>();
            for (int k = 0; k < lap.sectorMillis().size(); k++) {
                Long sector = lap.sectorMillis().get(k);
                isBest.add(lap.isValid() && sector != null && sector.equals(bestSectors.get(k)));
            }
            return new LapDTO(
                lap.number(), lap.startedAt(), lap.durationMillis(), lap.sectorMillis(), isBest,
                Math.round(lap.distanceMeters()), lap.validity(),
                best.map(b -> b.number() == lap.number()).orElse(false),
                lap.isValid() ? best.map(b -> lap.durationMillis() - b.durationMillis()).orElse(null) : null);
        }).toList();

        return new SessionDetailResponseDTO(
            s.id().value(), s.name(), s.startedAt(), s.endedAt(),
            new VenueDTO(s.layout().venueId().value(), s.layout().venueName()),
            s.layout().trackName(),
            new LayoutDTO(s.layout().layoutId().value(), s.layout().layoutName(),
                (int) Math.round(s.layout().geometry().lengthMeters()), s.timing().sectorCount(), s.timing().reversedDirection()),
            MotorbikeDTO.fromDomain(s.motorbike()),
            ConditionsDTO.fromDomain(s.conditions()),
            s.notes(),
            summary.personalBest(),
            best.map(b -> new BestLapDTO(b.number(), b.durationMillis())).orElse(null),
            ideal.flatMap(i -> best.map(b -> new IdealSummaryDTO(i.totalMillis(), b.durationMillis() - i.totalMillis()))).orElse(null),
            s.timing().laps().size(),
            s.timing().validLaps().size(),
            laps);
    }

    private static List<Long> bestSectors(Session s) {
        List<Long> best = new ArrayList<>();
        for (int k = 1; k <= s.timing().sectorCount(); k++) {
            int index = k;
            best.add(s.timing().validLaps().stream()
                .map(l -> l.sector(index).orElse(null))
                .filter(v -> v != null)
                .min(Long::compare)
                .orElse(null));
        }
        return best;
    }
}
