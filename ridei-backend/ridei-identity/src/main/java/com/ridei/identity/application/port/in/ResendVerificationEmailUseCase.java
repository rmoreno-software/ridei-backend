package com.ridei.identity.application.port.in;

import com.ridei.identity.application.ResendVerificationEmailCommand;

public interface ResendVerificationEmailUseCase {
    void resend(ResendVerificationEmailCommand command);
}
