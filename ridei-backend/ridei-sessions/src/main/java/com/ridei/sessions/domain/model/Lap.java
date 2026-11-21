package com.ridei.sessions.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Vuelta cronometrada: de un paso por meta al siguiente.
 *
 * @param sectorMillis tiempo de cada sector; un elemento es {@code null} si no se detectó el
 *                     paso por alguna de sus puertas
 */
public record Lap(
    int number,
    Instant startedAt,
    long durationMillis,
    List<Long> sectorMillis,
    double distanceMeters,
    LapValidity validity
) {
    public Lap {
        if (number < 1) throw new IllegalArgumentException("Lap numbers start at 1");
        if (startedAt == null) throw new IllegalArgumentException("Lap start is required");
        if (durationMillis <= 0) throw new IllegalArgumentException("Lap duration must be positive");
        if (sectorMillis == null || sectorMillis.isEmpty()) throw new IllegalArgumentException("Sector times are required");
        if (validity == null) throw new IllegalArgumentException("Lap validity is required");
        sectorMillis = Collections.unmodifiableList(new ArrayList<>(sectorMillis));
    }

    public boolean isValid() {
        return validity == LapValidity.VALID;
    }

    /** Todos los sectores cronometrados: la vuelta puede aportar a la vuelta ideal. */
    public boolean hasAllSectors() {
        return sectorMillis.stream().allMatch(s -> s != null);
    }

    public Optional<Long> sector(int index) {
        return Optional.ofNullable(sectorMillis.get(index - 1));
    }

    public Instant endedAt() {
        return startedAt.plusMillis(durationMillis);
    }
}
