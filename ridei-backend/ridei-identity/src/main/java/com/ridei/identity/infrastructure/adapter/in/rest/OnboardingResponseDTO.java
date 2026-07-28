package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OnboardingResponseDTO {
    private String message;

    public static OnboardingResponseDTO success() {
        return new OnboardingResponseDTO("Onboarding completed successfully");
    }
}
