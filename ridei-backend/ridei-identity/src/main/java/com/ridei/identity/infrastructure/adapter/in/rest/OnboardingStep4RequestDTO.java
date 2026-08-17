package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardingStep4RequestDTO {
    
    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;
    
}
