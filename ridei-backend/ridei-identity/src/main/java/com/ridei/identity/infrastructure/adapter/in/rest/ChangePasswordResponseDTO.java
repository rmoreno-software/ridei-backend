package com.ridei.identity.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ChangePasswordResponseDTO {
    private String accessToken;
    private String refreshToken;
}
