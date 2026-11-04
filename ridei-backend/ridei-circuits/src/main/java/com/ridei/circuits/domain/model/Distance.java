package com.ridei.circuits.domain.model;

public record Distance(double meters) implements Comparable<Distance> {

    /** Radio máximo admitido en búsquedas, para acotar el coste de las consultas geográficas. */
    public static final Distance MAX_SEARCH_RADIUS = ofKilometers(500);

    public Distance {
        if (!Double.isFinite(meters) || meters < 0) throw new IllegalArgumentException("Distance must be zero or positive");
    }

    public static Distance ofMeters(double meters) {
        return new Distance(meters);
    }

    public static Distance ofKilometers(double kilometers) {
        return new Distance(kilometers * 1000);
    }

    public static Distance searchRadiusOfKilometers(double kilometers) {
        Distance radius = ofKilometers(kilometers);
        if (radius.meters <= 0) throw new IllegalArgumentException("Radius must be positive");
        if (radius.compareTo(MAX_SEARCH_RADIUS) > 0) throw new IllegalArgumentException("Radius must be at most 500 km");
        return radius;
    }

    public boolean isWithin(Distance limit) {
        return meters <= limit.meters;
    }

    @Override
    public int compareTo(Distance other) {
        return Double.compare(meters, other.meters);
    }
}
