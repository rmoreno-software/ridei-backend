package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.VenueId;

public record MergeVenuesCommand(
    VenueId sourceId,
    VenueId targetId
) {
    public MergeVenuesCommand {
        if (sourceId == null || targetId == null) {
            throw new IllegalArgumentException("Source and target are required");
        }
    }
}
