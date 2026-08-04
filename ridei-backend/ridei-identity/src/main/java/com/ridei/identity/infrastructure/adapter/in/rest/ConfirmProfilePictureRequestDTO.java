package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfirmProfilePictureRequestDTO {
    
    @NotBlank(message = "Public URL is required")
    @Pattern(regexp = "^https://.+", message = "Must be a valid HTTPS URL")
    private String publicUrl;
}
