package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ResetPasswordCommand;

public interface ResetPasswordUseCase {
    void reset(ResetPasswordCommand command);
}
