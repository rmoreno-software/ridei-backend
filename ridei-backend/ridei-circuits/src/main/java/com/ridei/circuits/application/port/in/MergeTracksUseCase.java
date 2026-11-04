package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.MergeTracksCommand;
import com.ridei.circuits.domain.model.Venue;

public interface MergeTracksUseCase {

    Venue merge(MergeTracksCommand command);
}
