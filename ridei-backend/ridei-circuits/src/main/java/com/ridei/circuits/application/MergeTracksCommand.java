package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.TrackId;

public record MergeTracksCommand(
    TrackId sourceId,
    TrackId targetId
) {
    public MergeTracksCommand {
        if (sourceId == null || targetId == null) {
            throw new IllegalArgumentException("Source and target are required");
        }
    }
}
