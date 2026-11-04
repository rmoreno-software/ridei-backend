package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.LayoutSpec;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

public record AddLayoutToTrackCommand(
    VenueId venueId,
    TrackId trackId,
    LayoutSpec layout
) {
    public AddLayoutToTrackCommand {
        if (venueId == null || trackId == null || layout == null) {
            throw new IllegalArgumentException("Venue, track and layout are required");
        }
    }
}
