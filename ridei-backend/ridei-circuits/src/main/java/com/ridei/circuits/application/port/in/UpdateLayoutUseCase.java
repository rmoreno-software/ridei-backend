package com.ridei.circuits.application.port.in;

import com.ridei.circuits.application.UpdateLayoutCommand;
import com.ridei.circuits.domain.model.Layout;

public interface UpdateLayoutUseCase {

    Layout update(UpdateLayoutCommand command);
}
