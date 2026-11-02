package com.ridei.identity.application.port.in;

import com.ridei.identity.application.RequestTemporaryPasswordCommand;

public interface RequestTemporaryPasswordUseCase {
    void request(RequestTemporaryPasswordCommand command);
}
