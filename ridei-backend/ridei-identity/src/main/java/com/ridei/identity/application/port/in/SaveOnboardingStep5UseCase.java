package com.ridei.identity.application.port.in;

import com.ridei.identity.application.SaveOnboardingStep5Command;

public interface SaveOnboardingStep5UseCase {
    void complete(SaveOnboardingStep5Command command);
}
