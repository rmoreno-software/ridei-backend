package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.VerifyEmailCommand;

public interface VerifyEmailUseCase {
    void verify(VerifyEmailCommand command);
}
