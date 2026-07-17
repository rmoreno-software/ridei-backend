package com.ridei.identity.application;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.GoogleUserInfo;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.out.GoogleTokenVerifierPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class LoginWithGoogleService implements LoginWithGoogleUseCase{

    private final GoogleTokenVerifierPort googleVerifier;
    private final UserRepositoryPort repository;
    private final JwtPort jwt;

    private static final Logger log = LoggerFactory.getLogger(LoginWithGoogleService.class);

    public LoginWithGoogleService (
        GoogleTokenVerifierPort googleTokenVerifierPort,
        UserRepositoryPort userRepositoryPort,
        JwtPort jwtPort
    ) {
        this.googleVerifier = googleTokenVerifierPort;
        this.repository = userRepositoryPort;
        this.jwt = jwtPort;
    }

    @Override
    public GoogleAuthResult login(LoginWithGoogleCommand command) {
        GoogleUserInfo googleInfo = googleVerifier.verify(command.idToken());

        log.info("[LoginWithGoogleService - login] - googleInfo: {}", googleInfo);

        // Cerca primer per googleId, després per email (conta preexistent)
        Optional<User> existing = repository.findByGoogleId(googleInfo.googleId())
            .or(() -> repository.findByEmail(new Email(googleInfo.email())));

        User user = existing.orElseGet(() -> {
            User newUser = User.registerWithGoogle(googleInfo);
            repository.save(newUser);
            return newUser;
        });

        // Si existía por email pero notenía googleId, lo vinculamos
        if (user.getGoogleId() == null) {
            user.linkGoogleId(googleInfo.googleId());
            repository.save(user);
        }

        boolean needsOnboarding = user.getStatus() == AccountStatus.PENDING_ONBOARDING;

        return new GoogleAuthResult(
            jwt.generateAccessToken(user.getId(), user.getRole()),
            jwt.generateRefreshToken(user.getId()),
            needsOnboarding
        );
        
    }
    
}
