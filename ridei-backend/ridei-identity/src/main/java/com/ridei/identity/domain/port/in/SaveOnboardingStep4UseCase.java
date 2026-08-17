package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.SaveOnboardingStep4Command;

public interface SaveOnboardingStep4UseCase {
    void complete(SaveOnboardingStep4Command command);
}
