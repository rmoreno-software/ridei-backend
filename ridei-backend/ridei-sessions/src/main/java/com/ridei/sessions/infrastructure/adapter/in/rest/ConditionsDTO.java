package com.ridei.sessions.infrastructure.adapter.in.rest;

import com.ridei.sessions.domain.model.Conditions;
import com.ridei.sessions.domain.model.TrackCondition;

/** @param trackCondition DRY, DAMP o WET */
public record ConditionsDTO(Integer temperatureCelsius, TrackCondition trackCondition) {

    public Conditions toDomain() {
        return new Conditions(temperatureCelsius, trackCondition);
    }

    public static ConditionsDTO fromDomain(Conditions conditions) {
        return new ConditionsDTO(conditions.temperatureCelsius(), conditions.trackCondition());
    }
}
