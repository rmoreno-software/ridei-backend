package com.ridei.identity.infrastructure.adapter.in.rest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data 
public class RefreshTokenRequestDTO {
    @NotBlank (message = "{validation.refresh_token.required}")
    private String refreshToken;
}
