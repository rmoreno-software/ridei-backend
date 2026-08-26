package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ChangePasswordCommand;

public interface ChangePasswordUseCase {
    void change (ChangePasswordCommand command);
}
