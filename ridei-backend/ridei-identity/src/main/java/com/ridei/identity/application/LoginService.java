package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.LoginUseCase;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final JwtPort jwt;

    public LoginService(
        UserRepositoryPort userRepositoryPort,
        PasswordHasherPort passwordHasherPort,
        JwtPort jwt
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasherPort;
        this.jwt = jwt;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepositoryPort.findByEmail(command.email())
            .orElseThrow(InvalidCredentialException::new);

        boolean passwordMatches = user.getPasswordHash() != null
            && passwordHasherPort.matches(command.password(), user.getTemporaryPasswordHash());

        boolean usingTemporaryPassword = false;
        if (!passwordMatches && user.hasValidTemporaryPassword()
                && passwordHasherPort.matches(command.password(), user.getTemporaryPasswordHash())) {
            passwordMatches = true;
            usingTemporaryPassword = true;
        }

        if (!passwordMatches)
            throw new InvalidCredentialException();

        if (user.isSuspended())
            throw new UserSuspendedException();
        
        if (usingTemporaryPassword) {
            user.clearTemporaryPassword();;
            userRepositoryPort.update(user);
        }

        return new LoginResult(
            user.getId(),
            user.getEmail().value(),
            jwt.generateAccessToken(user.getId(), user.getRole()),
            jwt.generateRefreshToken(user.getId()),
            user.needsOnboarding(),
            usingTemporaryPassword
        );
    }
    
}
