package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.SetDefaultLayoutCommand;
import com.ridei.circuits.domain.model.Track;

public interface SetDefaultLayoutUseCase {

    Track setDefault(SetDefaultLayoutCommand command);
}
