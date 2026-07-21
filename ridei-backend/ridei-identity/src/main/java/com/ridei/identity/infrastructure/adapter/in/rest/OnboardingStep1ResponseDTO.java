package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingStep1ResponseDTO {
    private String message;

    public static OnboardingStep1ResponseDTO success() {
        return new OnboardingStep1ResponseDTO("Onboarding completed successfully");
    }
}
