package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RequestTemporaryPasswordCommand;

public interface RequestTemporaryPasswordUseCase {
    void request(RequestTemporaryPasswordCommand command);
}
