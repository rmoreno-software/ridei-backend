package com.ridei.identity.application;

import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.application.port.in.ValidateTokenUseCase;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

/**
 * Validación online de un access token, para los servicios que solo tienen la clave pública
 * (p. ej. ridei-circuits): firma y caducidad, cuenta activa y que el token no esté revocado,
 * es decir, que su {@code tv} coincida con el {@code tokenVersion} actual del usuario (sube al
 * cambiar o restablecer la contraseña).
 */
@AllArgsConstructor
public class ValidateTokenService implements ValidateTokenUseCase {

    private final JwtPort jwt;
    private final UserRepositoryPort repositoryPort;

    @Override
    public boolean validate(String token) {
        if (!jwt.validateAccessToken(token)) return false;

        try {
            UserId userId = jwt.extractUserId(token);
            int tokenVersion = jwt.extractTokenVersion(token);
            return repositoryPort.findById(userId)
                .filter(User::isActive)
                .filter(user -> user.getTokenVersion() == tokenVersion)
                .isPresent();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
