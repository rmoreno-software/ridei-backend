package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class ResendVerificationEmailRequestDTO {
    @NotBlank(message = "Email is required")
    private String email;
}
