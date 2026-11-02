package com.ridei.identity.application;

import java.time.Instant;
import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class RegisterUserService implements RegisterUserUseCase {

    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordHasherPort passwordHasher;
    private final JwtPort jwt;
    private final EmailSenderPort emailSender;
    private final String publicApiUrl;
    private final EmailVerificationTokenFactory tokenFactory = new EmailVerificationTokenFactory();

    public RegisterUserService(
        UserRepositoryPort userRepository,
        EventPublisherPort eventPublisher,
        PasswordHasherPort passwordHasher,
        JwtPort jwt,
        EmailSenderPort emailSender,
        String publicApiUrl
    ) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.passwordHasher = passwordHasher;
        this.jwt = jwt;
        this.emailSender = emailSender;
        this.publicApiUrl = publicApiUrl;
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyRegisteredException(command.email());

        User user = User.register(
            command.email(),
            passwordHasher.hash(command.password())
        );

        EmailVerificationTokenFactory.IssuedToken token = tokenFactory.issue();
        user.issueEmailVerificationToken(token.hashToken(), token.expiresAt());

        userRepository.save(user);

        eventPublisher.publish(new UserRegisteredEvent(user.getId(), user.getRole(), Instant.now()));

        String verificationLink =  tokenFactory.buildVerificationLink(publicApiUrl, token.rawToken(), command.locale());
        emailSender.sendEmailVerificationLink(user.getEmail(), verificationLink, command.locale());

        return new RegisterResult(
            user.getId(),
            user.getEmail().value(),
            jwt.generateAccessToken(user.getId(), user.getRole(), user.getTokenVersion()),
            jwt.generateRefreshToken(user.getId(), user.getTokenVersion()),
            user.needsOnboarding()
        );
    }
}
