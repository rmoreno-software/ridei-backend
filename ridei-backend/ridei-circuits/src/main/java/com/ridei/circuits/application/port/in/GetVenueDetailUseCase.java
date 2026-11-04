package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.VenueDetail;
import com.ridei.circuits.domain.model.VenueId;

public interface GetVenueDetailUseCase {

    VenueDetail get(VenueId venueId);
}
