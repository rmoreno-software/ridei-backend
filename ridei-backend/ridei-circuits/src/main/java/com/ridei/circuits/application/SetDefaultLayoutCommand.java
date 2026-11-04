package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

public record SetDefaultLayoutCommand(
    VenueId venueId,
    TrackId trackId,
    LayoutId layoutId
) {
    public SetDefaultLayoutCommand {
        if (venueId == null || trackId == null || layoutId == null) {
            throw new IllegalArgumentException("Venue, track and layout are required");
        }
    }
}
