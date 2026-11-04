package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.AddTrackToVenueCommand;
import com.ridei.circuits.domain.model.Track;

public interface AddTrackToVenueUseCase {

    Track addTrack(AddTrackToVenueCommand command);
}
