package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.TrackCorrection;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

public record UpdateTrackCommand(
    VenueId venueId,
    TrackId trackId,
    TrackCorrection correction
) {
    public UpdateTrackCommand {
        if (venueId == null || trackId == null || correction == null) {
            throw new IllegalArgumentException("Venue, track and correction are required");
        }
    }
}
