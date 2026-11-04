package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.UpdateTrackCommand;
import com.ridei.circuits.domain.model.Track;

public interface UpdateTrackUseCase {

    Track update(UpdateTrackCommand command);
}
