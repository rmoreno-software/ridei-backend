package com.ridei.circuits.domain.exception;

import com.ridei.circuits.domain.model.VenueId;

public class VenueNotFoundException extends RuntimeException {

    public VenueNotFoundException(VenueId id) {
        super("Venue not found: " + id);
    }
}
