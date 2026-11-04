package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.LayoutCorrection;
import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

public record UpdateLayoutCommand(
    VenueId venueId,
    TrackId trackId,
    LayoutId layoutId,
    LayoutCorrection correction
) {
    public UpdateLayoutCommand {
        if (venueId == null || trackId == null || layoutId == null || correction == null) {
            throw new IllegalArgumentException("Venue, track, layout and correction are required");
        }
    }
}
