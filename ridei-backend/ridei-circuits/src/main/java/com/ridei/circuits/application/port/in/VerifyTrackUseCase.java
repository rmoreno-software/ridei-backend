package com.ridei.circuits.application.port.in;

import com.ridei.circuits.domain.model.TrackId;
import com.ridei.circuits.domain.model.Venue;

public interface VerifyTrackUseCase {

    Venue verify(TrackId trackId);
}
