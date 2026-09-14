package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class ResendVerificationEmailRequestDTO {
    @NotBlank(message = "{validation.email.required}")
    private String email;
}
