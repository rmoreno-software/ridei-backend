package com.ridei.sessions.application.port.in;

import com.ridei.sessions.application.EditSessionCommand;
import com.ridei.sessions.domain.model.Session;

public interface EditSessionUseCase {
    Session edit(EditSessionCommand command);
}
