package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.FindNearbyVenuesQuery;
import com.ridei.circuits.domain.model.PageResult;
import com.ridei.circuits.domain.model.VenueSearchHit;

public interface FindNearbyVenuesUseCase {

    PageResult<VenueSearchHit> findNearby(FindNearbyVenuesQuery query);
}
