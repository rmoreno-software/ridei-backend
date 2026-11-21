package com.ridei.sessions.domain.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Vuelta ideal "cosida": el mejor tiempo de cada sector, cada uno de la vuelta en la que se
 * hizo. Es lo que el piloto ya sabe hacer, solo que no todo en la misma vuelta.
 */
public record IdealLap(long totalMillis, List<IdealSector> sectors) {

    /**
     * @param fromLap vuelta de la que sale el mejor sector
     */
    public record IdealSector(int index, long millis, int fromLap) {}

    public IdealLap {
        sectors = List.copyOf(sectors);
    }

    /** Vacío si ninguna vuelta tiene todos los sectores cronometrados. */
    public static Optional<IdealLap> from(List<Lap> laps, int sectorCount) {
        List<Lap> complete = laps.stream().filter(Lap::hasAllSectors).toList();
        if (complete.isEmpty()) return Optional.empty();
        List<IdealSector> sectors = new ArrayList<>(sectorCount);
        long total = 0;
        for (int index = 1; index <= sectorCount; index++) {
            int i = index;
            Lap best = complete.stream()
                .min(Comparator.comparingLong((Lap lap) -> lap.sector(i).orElseThrow()).thenComparingInt(Lap::number))
                .orElseThrow();
            long millis = best.sector(i).orElseThrow();
            sectors.add(new IdealSector(i, millis, best.number()));
            total += millis;
        }
        return Optional.of(new IdealLap(total, sectors));
    }

    public IdealSector sector(int index) {
        return sectors.get(index - 1);
    }
}
