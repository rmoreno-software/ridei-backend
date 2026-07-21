package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.SaveOnboardingStep1Command;

public interface SaveOnboardingStep1UseCase {
    void complete(SaveOnboardingStep1Command command);
}
