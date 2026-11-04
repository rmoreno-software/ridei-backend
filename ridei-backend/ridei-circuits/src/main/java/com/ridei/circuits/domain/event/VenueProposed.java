package com.ridei.circuits.domain.event;

import java.time.Instant;

import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.VenueId;
import com.ridei.circuits.domain.model.VenueName;

public record VenueProposed(
    VenueId venueId,
    VenueName name,
    CountryCode countryCode,
    GeoPoint location,
    UserId proposedBy,
    Instant occurredAt
) implements DomainEvent {
}
