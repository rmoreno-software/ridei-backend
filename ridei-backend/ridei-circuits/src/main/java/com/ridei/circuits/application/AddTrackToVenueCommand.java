package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.TrackSpec;
import com.ridei.circuits.domain.model.VenueId;

public record AddTrackToVenueCommand(
    VenueId venueId,
    TrackSpec track
) {
    public AddTrackToVenueCommand {
        if (venueId == null || track == null) {
            throw new IllegalArgumentException("Venue and track are required");
        }
    }
}
