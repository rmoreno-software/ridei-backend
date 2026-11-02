package com.ridei.landing.application.port.in;

import com.ridei.landing.application.JoinWaitlistCommand;

public interface JoinWaitlistUseCase {
    void join(JoinWaitlistCommand command);
}
