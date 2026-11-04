package com.ridei.circuits.domain.model;

import java.util.UUID;

public record TrackId(UUID value) {

    public TrackId {
        if (value == null) throw new IllegalArgumentException("Track id is required");
    }

    public static TrackId newId() {
        return new TrackId(UUID.randomUUID());
    }

    public static TrackId of(String value) {
        return new TrackId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
