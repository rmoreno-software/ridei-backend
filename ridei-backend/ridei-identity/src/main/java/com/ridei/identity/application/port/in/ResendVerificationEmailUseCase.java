package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ResendVerificationEmailCommand;

public interface ResendVerificationEmailUseCase {
    void resend(ResendVerificationEmailCommand command);
}
