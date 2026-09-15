package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.LoginUseCase;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final JwtPort jwt;

    public LoginService(
        UserRepositoryPort userRepository,
        PasswordHasherPort passwordHasher,
        JwtPort jwt
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwt = jwt;
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(InvalidCredentialException::new);

        boolean passwordMatches = user.getPasswordHash() != null
            && passwordHasher.matches(command.password(), user.getPasswordHash());

        boolean usingTemporaryPassword = false;
        if (!passwordMatches && user.hasValidTemporaryPassword()
                && passwordHasher.matches(command.password(), user.getTemporaryPasswordHash())) {
            passwordMatches = true;
            usingTemporaryPassword = true;
        }

        if (!passwordMatches)
            throw new InvalidCredentialException();

        if (user.isSuspended())
            throw new UserSuspendedException();
        
        if (usingTemporaryPassword) {
            user.clearTemporaryPassword();
            userRepository.update(user);
        }

        return new LoginResult(
            user.getId(),
            user.getEmail().value(),
            jwt.generateAccessToken(user.getId(), user.getRole(), user.getTokenVersion()),
            jwt.generateRefreshToken(user.getId(), user.getTokenVersion()),
            user.needsOnboarding(),
            usingTemporaryPassword
        );
    }
    
}
