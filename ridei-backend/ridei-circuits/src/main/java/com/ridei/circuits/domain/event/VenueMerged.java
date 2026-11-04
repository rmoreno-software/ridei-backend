package com.ridei.circuits.domain.event;

import java.time.Instant;
import java.util.List;

import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.VenueId;

/**
 * El venue {@code source} es un duplicado de {@code target}. Todos sus tracks
 * ({@code movedTrackIds}) pertenecen ahora a {@code target}; quien referencie
 * {@code source} debe reasignarlo a {@code target}.
 */
public record VenueMerged(
    VenueId source,
    VenueId target,
    List<TrackId> movedTrackIds,
    Instant occurredAt
) implements DomainEvent {

    public VenueMerged {
        movedTrackIds = List.copyOf(movedTrackIds);
    }
}
