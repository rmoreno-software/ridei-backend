package com.ridei.garage.domain.model;

public enum Category {
    SPEED(CategoryGroup.ASPHALT),
    NAKED(CategoryGroup.ASPHALT),
    CLASSIC(CategoryGroup.ASPHALT),
    SIDECAR(CategoryGroup.ASPHALT),
    SCOOTER(CategoryGroup.ASPHALT),
    MINIBIKE(CategoryGroup.ASPHALT),

    ENDURO(CategoryGroup.OFF_ROAD),
    MOTOCROSS(CategoryGroup.OFF_ROAD),
    RALLY(CategoryGroup.OFF_ROAD),
    DIRT_TRACK(CategoryGroup.OFF_ROAD),
    SPEEDWAY(CategoryGroup.OFF_ROAD),
    SIDECARCROSS(CategoryGroup.OFF_ROAD),

    SUPERMOTARD(CategoryGroup.MIXED),
    TRAIL(CategoryGroup.MIXED),
    PITBIKE(CategoryGroup.MIXED);

    private final CategoryGroup group;

    Category(CategoryGroup group) {
        this.group = group;
    }

    public CategoryGroup group() {
        return group;
    }
}
