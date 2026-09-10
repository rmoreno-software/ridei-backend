package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.VerifyEmailCommand;

public interface VerifyEmailUseCase {
    boolean isTokenValid(String token);
    void verify(VerifyEmailCommand command);
}
