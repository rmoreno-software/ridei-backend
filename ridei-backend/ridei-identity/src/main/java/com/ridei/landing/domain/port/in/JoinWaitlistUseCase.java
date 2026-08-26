package com.ridei.landing.domain.port.in;

import com.ridei.landing.application.JoinWaitlistCommand;

public interface JoinWaitlistUseCase {
    void join(JoinWaitlistCommand command);
}
