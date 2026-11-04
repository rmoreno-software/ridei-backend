package com.ridei.circuits.application;

import com.ridei.circuits.domain.model.Distance;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.PageRequest;

public record FindNearbyVenuesQuery(
    GeoPoint location,
    Distance radius,
    PageRequest page
) {
    public FindNearbyVenuesQuery {
        if (location == null || radius == null || page == null) {
            throw new IllegalArgumentException("Location, radius and page are required");
        }
    }
}
