package com.ridei.sessions.domain.model;

import java.util.UUID;

public record VenueId(UUID value) {
    public VenueId {
        if (value == null) throw new IllegalArgumentException("Venue id is required");
    }

    public static VenueId of(String value) {
        try {
            return new VenueId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Venue id must be a UUID");
        }
    }

    public static VenueId of(UUID value) {
        return new VenueId(value);
    }
}
