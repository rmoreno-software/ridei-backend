package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.CountryCode;
import com.ridei.circuits.domain.model.ExternalPlaceId;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.LayoutSpec;
import com.ridei.circuits.domain.model.TrackSpec;
import com.ridei.circuits.domain.model.UserId;
import com.ridei.circuits.domain.model.VenueName;

public record ProposeVenueCommand(
    UserId proposedBy,
    VenueName name,
    CountryCode countryCode,
    GeoPoint location,
    ExternalPlaceId externalPlaceId,
    TrackSpec firstTrack,
    LayoutSpec firstLayout,
    boolean confirmedNotDuplicate
) {
    public ProposeVenueCommand {
        if (proposedBy == null || name == null || countryCode == null || location == null || firstTrack == null) {
            throw new IllegalArgumentException("Proposer, name, country, location and first track are required");
        }
    }
}
