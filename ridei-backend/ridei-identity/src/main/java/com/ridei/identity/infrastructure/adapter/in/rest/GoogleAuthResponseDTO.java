package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoogleAuthResponseDTO {
    private String accessToken;
    private String refreshToken;
    private boolean needsOnboarding;
}
