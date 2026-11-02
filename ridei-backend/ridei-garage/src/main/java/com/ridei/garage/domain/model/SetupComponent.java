package com.ridei.garage.domain.model;

/**
 * Pieza del setup de una moto que se sigue con histórico propio.
 * Cada componente acepta un único tipo de especificación.
 */
public enum SetupComponent {
    FRONT_TYRE,
    REAR_TYRE,
    FINAL_DRIVE;

    public boolean accepts(SetupSpec spec) {
        return switch (this) {
            case FRONT_TYRE, REAR_TYRE -> spec instanceof TyreSpec;
            case FINAL_DRIVE -> spec instanceof FinalDriveRatio;
        };
    }
}
