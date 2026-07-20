package com.ridei.identity.application;

import java.time.Instant;
import java.util.Optional;

import com.ridei.identity.domain.event.UserGoogleAccountLinkedEvent;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.GoogleUserInfo;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.LoginWithGoogleUseCase;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.GoogleTokenVerifierPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class LoginWithGoogleService implements LoginWithGoogleUseCase{

    private final GoogleTokenVerifierPort googleVerifier;
    private final UserRepositoryPort repository;
    private final JwtPort jwt;
    private final EventPublisherPort eventPublisher;

    public LoginWithGoogleService (
        GoogleTokenVerifierPort googleTokenVerifierPort,
        UserRepositoryPort userRepositoryPort,
        JwtPort jwtPort,
        EventPublisherPort eventPublisherPort
    ) {
        this.googleVerifier = googleTokenVerifierPort;
        this.repository = userRepositoryPort;
        this.jwt = jwtPort;
        this.eventPublisher = eventPublisherPort;
    }

    @Override
    public GoogleAuthResult login(LoginWithGoogleCommand command) {
        GoogleUserInfo googleInfo = googleVerifier.verify(command.idToken());

        // Cerca primer per googleId, després per email (conta preexistent)
        Optional<User> existing = repository.findByGoogleId(googleInfo.googleId())
            .or(() -> repository.findByEmail(new Email(googleInfo.email())));

        User user = existing.orElseGet(() -> {
            User newUser = User.registerWithGoogle(googleInfo);
            repository.save(newUser);
            return newUser;
        });

        // Si existía por email pero no tenía googleId, lo vinculamos
        if (user.getGoogleId() == null) {
            user.linkGoogleId(googleInfo.googleId());
            repository.save(user);
            eventPublisher.publish(new UserGoogleAccountLinkedEvent(user.getId(), googleInfo.googleId(), Instant.now()));
        }

        return new GoogleAuthResult(
            jwt.generateAccessToken(user.getId(), user.getRole()),
            jwt.generateRefreshToken(user.getId()),
            user.needsOnboarding()
        );

    }

}
