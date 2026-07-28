package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.SaveOnboardingStep2Command;

public interface SaveOnboardingStep2UseCase {
    void complete(SaveOnboardingStep2Command command);
}
