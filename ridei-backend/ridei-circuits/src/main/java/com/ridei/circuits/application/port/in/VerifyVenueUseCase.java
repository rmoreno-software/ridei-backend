package com.ridei.circuits.application.port.in;

import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

public interface VerifyVenueUseCase {

    Venue verify(VenueId venueId);
}
