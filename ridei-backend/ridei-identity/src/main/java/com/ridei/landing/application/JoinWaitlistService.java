package com.ridei.landing.application;

import com.ridei.landing.domain.model.WaitlistEntry;
import com.ridei.landing.domain.port.in.JoinWaitlistUseCase;
import com.ridei.landing.domain.port.in.WaitlistRepositoryPort;

public class JoinWaitlistService implements JoinWaitlistUseCase {

    private final WaitlistRepositoryPort waitlistRepositoryPort;

    public JoinWaitlistService(WaitlistRepositoryPort waitlistRepositoryPort) {
        this.waitlistRepositoryPort = waitlistRepositoryPort;
    }

    @Override
    public void join(JoinWaitlistCommand command) {
        if (waitlistRepositoryPort.existsByEmail(command.email()))
            return;

        waitlistRepositoryPort.save(WaitlistEntry.join(command.email()));
    }
    
}
