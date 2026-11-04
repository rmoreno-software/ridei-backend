package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.UpdateVenueCommand;
import com.ridei.circuits.domain.model.Venue;

public interface UpdateVenueUseCase {

    Venue update(UpdateVenueCommand command);
}
