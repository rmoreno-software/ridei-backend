package com.ridei.circuits.domain.model;

import java.util.UUID;

public record VenueId(UUID value) {

    public VenueId {
        if (value == null) throw new IllegalArgumentException("Venue id is required");
    }

    public static VenueId newId() {
        return new VenueId(UUID.randomUUID());
    }

    public static VenueId of(String value) {
        return new VenueId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
