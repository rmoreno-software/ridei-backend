package com.ridei.circuits.domain.event;

import java.time.Instant;
import java.util.List;

import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

/**
 * El track {@code source} es un duplicado de {@code target} (que vive en {@code targetVenue}).
 * Los layouts no equivalentes ({@code movedLayoutIds}) se han movido a {@code target}; los
 * equivalentes se notifican con {@link LayoutMerged}.
 */
public record TrackMerged(
    TrackId source,
    TrackId target,
    VenueId targetVenue,
    List<LayoutId> movedLayoutIds,
    Instant occurredAt
) implements DomainEvent {

    public TrackMerged {
        movedLayoutIds = List.copyOf(movedLayoutIds);
    }
}
