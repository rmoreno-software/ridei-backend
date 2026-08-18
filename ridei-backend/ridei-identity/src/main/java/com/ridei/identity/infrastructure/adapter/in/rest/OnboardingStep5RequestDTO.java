package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class OnboardingStep5RequestDTO {
    
    @AssertTrue(message = "You must accept the terms and conditions")
    private boolean termsAccepted;
    
}
