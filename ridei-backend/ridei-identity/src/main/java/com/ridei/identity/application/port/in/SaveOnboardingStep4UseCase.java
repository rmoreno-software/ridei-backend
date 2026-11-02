package com.ridei.identity.application.port.in;

import com.ridei.identity.application.SaveOnboardingStep4Command;

public interface SaveOnboardingStep4UseCase {
    void complete(SaveOnboardingStep4Command command);
}
