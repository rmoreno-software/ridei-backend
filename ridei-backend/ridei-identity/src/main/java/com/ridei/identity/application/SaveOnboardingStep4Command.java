package com.ridei.identity.application;

import com.ridei.identity.domain.model.IdentityDocument;
import com.ridei.identity.domain.model.UserId;

public record SaveOnboardingStep4Command(
    UserId userId,
    IdentityDocument identityDocument
) {
    
}
