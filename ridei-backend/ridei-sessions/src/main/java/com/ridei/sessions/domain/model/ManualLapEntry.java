package com.ridei.sessions.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lo que el piloto apunta de una tanda pasada (pantalla 22B). Solo la fecha, la mejor vuelta y
 * el origen son obligatorios; el resto es "Más datos · opcional".
 *
 * @param bestLapMillis   mejor vuelta de la tanda
 * @param precision       con cuánta precisión se apuntó; el tiempo debe encajar (1:49.8 no puede
 *                        tener milésimas)
 * @param lapCount        vueltas dadas en la tanda, si se sabe
 * @param otherLapsMillis otras vueltas de la misma tanda, ninguna mejor que {@code bestLapMillis}
 */
public record ManualLapEntry(
    LocalDate date,
    long bestLapMillis,
    LapTimePrecision precision,
    TimeSource source,
    Integer lapCount,
    TrackCondition trackCondition,
    String tyres,
    List<Long> otherLapsMillis,
    String notes
) {
    public static final long MIN_LAP_MILLIS = 10_000;
    public static final long MAX_LAP_MILLIS = 60 * 60_000;
    public static final LocalDate EARLIEST_DATE = LocalDate.of(1950, 1, 1);
    public static final int MAX_LAP_COUNT = 500;
    public static final int MAX_OTHER_LAPS = 100;
    public static final int MAX_TYRES_LENGTH = 100;
    public static final int MAX_NOTES_LENGTH = 2000;

    public ManualLapEntry {
        if (date == null) throw new IllegalArgumentException("Date is required");
        if (date.isBefore(EARLIEST_DATE)) throw new IllegalArgumentException("Date must be on or after " + EARLIEST_DATE);
        requireLapTime(bestLapMillis);
        if (source == null) throw new IllegalArgumentException("Time source is required");
        if (precision == null) precision = LapTimePrecision.inferFrom(bestLapMillis);
        if (!precision.fits(bestLapMillis)) {
            throw new IllegalArgumentException("Best lap has more precision than " + precision.name().toLowerCase());
        }

        List<Long> others = new ArrayList<>();
        if (otherLapsMillis != null) {
            if (otherLapsMillis.size() > MAX_OTHER_LAPS) throw new IllegalArgumentException("At most " + MAX_OTHER_LAPS + " other laps");
            for (Long lap : otherLapsMillis) {
                if (lap == null) throw new IllegalArgumentException("Other laps cannot be empty");
                requireLapTime(lap);
                if (lap < bestLapMillis) throw new IllegalArgumentException("Other laps cannot be faster than the best lap");
                others.add(lap);
            }
        }
        otherLapsMillis = List.copyOf(others);

        if (lapCount != null) {
            if (lapCount < 1 || lapCount > MAX_LAP_COUNT) throw new IllegalArgumentException("Lap count must be between 1 and " + MAX_LAP_COUNT);
            if (lapCount < 1 + otherLapsMillis.size()) throw new IllegalArgumentException("Lap count is lower than the laps given");
        }
        tyres = blankToNull(tyres);
        if (tyres != null && tyres.length() > MAX_TYRES_LENGTH) throw new IllegalArgumentException("Tyres must be at most " + MAX_TYRES_LENGTH + " characters");
        notes = blankToNull(notes);
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) throw new IllegalArgumentException("Notes must be at most " + MAX_NOTES_LENGTH + " characters");
    }

    private static void requireLapTime(long millis) {
        if (millis < MIN_LAP_MILLIS || millis > MAX_LAP_MILLIS) {
            throw new IllegalArgumentException("Lap times must be between 10 s and 60 min");
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
