package com.ridei.sessions.domain.model;

/**
 * Con cuánta precisión se apuntó un tiempo manual ("1:52", "1:49.8", "1:14.22", "1:45.300"). Sirve
 * para mostrarlo tal cual se apuntó, sin inventar milésimas.
 */
public enum LapTimePrecision {
    SECONDS(1000),
    TENTHS(100),
    HUNDREDTHS(10),
    THOUSANDTHS(1);

    private final long stepMillis;

    LapTimePrecision(long stepMillis) {
        this.stepMillis = stepMillis;
    }

    public long stepMillis() {
        return stepMillis;
    }

    public boolean fits(long millis) {
        return millis % stepMillis == 0;
    }

    /** @param digits cifras tras el separador decimal (0–3) */
    public static LapTimePrecision ofFractionDigits(int digits) {
        return switch (digits) {
            case 0 -> SECONDS;
            case 1 -> TENTHS;
            case 2 -> HUNDREDTHS;
            case 3 -> THOUSANDTHS;
            default -> throw new IllegalArgumentException("A lap time has at most 3 decimals");
        };
    }

    /** La menos precisa compatible con el valor: 105300 → décimas. */
    public static LapTimePrecision inferFrom(long millis) {
        for (LapTimePrecision p : values()) {
            if (p.fits(millis)) return p;
        }
        return THOUSANDTHS;
    }
}
