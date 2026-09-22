package com.ridei.garage.domain.service;

import java.util.List;

import com.ridei.garage.domain.model.Motorbike;

public final class MotorbikeActivationPolicy {
    
    private MotorbikeActivationPolicy() {}

    /**
     * Enforces "at most one active motorbike per categoryGroup, per owner":
     * deactivates every motorbike in {@code siblings} that is currently active
     * and shares {@code target}'s categoryGroup. Does not touch {@code target}
     * itself — call {@code target.activate()} separately.
     *
     * @return the motorbikes that were deactivated as a side effect (the only ones the caller needs to persist)
     */
    public static List<Motorbike> deactivateConflictsWith(
        Motorbike target,
        List<Motorbike> siblings
    ) {
        List<Motorbike> conflicting = siblings.stream()
            .filter(m -> !m.getId().equals(target.getId()))
            .filter(Motorbike::isActive)
            .filter(m -> m.getCategoryGroup() == target.getCategoryGroup())
            .toList();
        
            conflicting.forEach(Motorbike::deactivate);

            return conflicting;
    }
}
