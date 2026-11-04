package com.ridei.circuits.domain.event;

import java.time.Instant;

import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.TrackName;
import com.ridei.circuits.domain.model.VenueId;

public record TrackAdded(
    VenueId venueId,
    TrackId trackId,
    Discipline discipline,
    TrackName name,
    Instant occurredAt
) implements DomainEvent {
}
