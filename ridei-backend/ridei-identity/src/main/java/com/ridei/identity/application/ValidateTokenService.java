package com.ridei.identity.application;

import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ValidateTokenService implements ValidateTokenUseCase {

    private final JwtPort jwt;
    private final UserRepositoryPort repositoryPort;

    @Override
    public boolean validate(String token) {
        if (!jwt.validateToken(token)) return false;

        try {
            UserId userId = jwt.extractUserId(token);
            return repositoryPort.findById(userId)
                .map(user -> user.getStatus() == AccountStatus.ACTIVE)
                .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
    
}
