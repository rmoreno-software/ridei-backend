package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ConfirmProfilePictureRequestDTO {
    
    @NotBlank(message = "{validation.public_url.required}")
    @Pattern(regexp = "^https://.+", message = "{validation.public_url.invalid}")
    private String publicUrl;
}
