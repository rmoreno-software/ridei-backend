package com.ridei.circuits.domain.event;

import java.time.Instant;

import com.ridei.circuits.domain.model.LayoutId;
import com.ridei.circuits.domain.model.LayoutName;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

public record LayoutAdded(
    VenueId venueId,
    TrackId trackId,
    LayoutId layoutId,
    LayoutName name,
    boolean isDefault,
    Instant occurredAt
) implements DomainEvent {
}
