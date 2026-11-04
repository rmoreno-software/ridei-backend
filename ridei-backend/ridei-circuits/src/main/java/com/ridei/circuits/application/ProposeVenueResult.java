package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.Venue;

/** Venue recién creado, o el existente con el mismo {@code externalPlaceId}. */
public record ProposeVenueResult(Venue venue, Outcome outcome) {

    public enum Outcome {
        CREATED,
        EXISTING_BY_EXTERNAL_PLACE_ID
    }

    public static ProposeVenueResult created(Venue venue) {
        return new ProposeVenueResult(venue, Outcome.CREATED);
    }

    public static ProposeVenueResult existing(Venue venue) {
        return new ProposeVenueResult(venue, Outcome.EXISTING_BY_EXTERNAL_PLACE_ID);
    }

    public boolean isCreated() {
        return outcome == Outcome.CREATED;
    }
}
