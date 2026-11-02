package com.ridei.identity.application.port.in;

import com.ridei.identity.application.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void reset(ResetPasswordCommand command);
}
