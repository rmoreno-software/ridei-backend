package com.ridei.garage.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Relación final: dientes del piñón (delante) y de la corona (detrás), p. ej. 16 / 48. */
public record FinalDriveRatio(int frontSprocket, int rearSprocket) implements SetupSpec {

    public static final int MAX_TEETH = 99;

    public FinalDriveRatio {
        requireTeeth(frontSprocket, "Front sprocket");
        requireTeeth(rearSprocket, "Rear sprocket");
    }

    /** Desmultiplicación corona / piñón, con 3 decimales (16 / 48 → 3.000). */
    public BigDecimal ratio() {
        return BigDecimal.valueOf(rearSprocket)
            .divide(BigDecimal.valueOf(frontSprocket), 3, RoundingMode.HALF_UP);
    }

    private static void requireTeeth(int teeth, String field) {
        if (teeth <= 0 || teeth > MAX_TEETH)
            throw new IllegalArgumentException(field + " teeth must be between 1 and " + MAX_TEETH);
    }
}
