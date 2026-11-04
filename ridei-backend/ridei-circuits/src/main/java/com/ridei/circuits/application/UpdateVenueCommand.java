package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.VenueCorrection;
import com.ridei.circuits.domain.model.VenueId;

public record UpdateVenueCommand(
    VenueId venueId,
    VenueCorrection correction
) {
    public UpdateVenueCommand {
        if (venueId == null || correction == null) {
            throw new IllegalArgumentException("Venue and correction are required");
        }
    }
}
