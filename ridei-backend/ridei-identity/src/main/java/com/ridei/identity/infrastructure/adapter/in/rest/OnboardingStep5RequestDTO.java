package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class OnboardingStep5RequestDTO {
    
    @AssertTrue(message = "{validation.terms.required}")
    private boolean termsAccepted;
    
}
