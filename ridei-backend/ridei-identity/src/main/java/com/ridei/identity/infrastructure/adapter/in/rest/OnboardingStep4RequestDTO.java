package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OnboardingStep4RequestDTO {
    
    @NotBlank(message = "{validation.document_type.required}")
    private String documentType;

    @NotBlank(message = "{validation.document_number.required}")
    private String documentNumber;
    
}
