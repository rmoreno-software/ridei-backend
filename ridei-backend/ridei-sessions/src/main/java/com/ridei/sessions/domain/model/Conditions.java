package com.ridei.sessions.domain.model;

/** Condiciones de la tanda; todo opcional. */
public record Conditions(Integer temperatureCelsius, TrackCondition trackCondition) {

    public static final Conditions UNKNOWN = new Conditions(null, null);

    public Conditions {
        if (temperatureCelsius != null && (temperatureCelsius < -30 || temperatureCelsius > 70)) {
            throw new IllegalArgumentException("Temperature must be between -30 and 70 °C");
        }
    }
}
