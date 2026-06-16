package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponseDTO {
    private String userId;
    private String message;

    public RegisterResponseDTO(String userId) {
        this.userId = userId;
        this.message = "User registered successfully. Please verify your email.";
    }
}
