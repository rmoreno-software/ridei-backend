package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.AddLayoutToTrackCommand;
import com.ridei.circuits.domain.model.Layout;

public interface AddLayoutToTrackUseCase {

    Layout addLayout(AddLayoutToTrackCommand command);
}
