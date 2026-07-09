package com.ridei.identity.application;

import com.ridei.identity.domain.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.out.JwtPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateTokenService implements ValidateTokenUseCase {

    private final JwtPort jwt;

    @Override
    public boolean validate(String token) {
        return jwt.validateToken(token);
    }
    
}
