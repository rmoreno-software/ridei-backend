package com.ridei.garage.domain.model;

public record FocalPoint(
    double x,
    double y
) {
    public static final FocalPoint CENTER = new FocalPoint(0.5, 0.5);

    public FocalPoint {
        if (!isUnit(x) || !isUnit(y))
            throw new IllegalArgumentException("Focal point coordinates must be between 0 and 1");
    }

    private static boolean isUnit(double value) {
        return value >= 0 && value <= 1;
    }
}
