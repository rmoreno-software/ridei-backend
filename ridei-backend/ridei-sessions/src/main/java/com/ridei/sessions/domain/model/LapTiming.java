package com.ridei.sessions.domain.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Resultado de cronometrar una tanda sobre un trazado.
 *
 * @param reversedDirection la tanda se rodó en sentido contrario al del catálogo; las vueltas se
 *                          cronometran igual, pero conviene revisar el sentido del trazado
 */
public record LapTiming(List<Lap> laps, int sectorCount, boolean reversedDirection) {

    public LapTiming {
        laps = List.copyOf(laps);
        if (sectorCount < 1) throw new IllegalArgumentException("Sector count must be positive");
    }

    public List<Lap> validLaps() {
        return laps.stream().filter(Lap::isValid).toList();
    }

    public Optional<Lap> bestLap() {
        return validLaps().stream().min(Comparator.comparingLong(Lap::durationMillis));
    }

    public Optional<Lap> lap(int number) {
        return laps.stream().filter(l -> l.number() == number).findFirst();
    }

    /** Mejor tiempo de cada sector entre las vueltas válidas. */
    public Optional<IdealLap> idealLap() {
        return IdealLap.from(validLaps(), sectorCount);
    }
}
