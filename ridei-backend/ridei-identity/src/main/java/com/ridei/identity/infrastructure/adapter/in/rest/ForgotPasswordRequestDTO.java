package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    @NotBlank
    private String email;
}
