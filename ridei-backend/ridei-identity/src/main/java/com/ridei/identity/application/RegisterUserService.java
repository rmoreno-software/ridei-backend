package com.ridei.identity.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.model.RegisterResult;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

public class RegisterUserService implements RegisterUserUseCase {

    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofHours(24);
    
    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordHasherPort passwordHasher;
    private final JwtPort jwt;
    private final EmailSenderPort emailSender;
    private final String publicApiUrl;
    private final SecureRandom random = new SecureRandom();

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
    public RegisterResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyRegisteredException(command.email());

        User user = User.register(
            command.email(),
            passwordHasher.hash(command.password())
        );

        String verificationToken = generateVerificationToken();
        user.issueEmailVerificationToken(
            TokenHasher.sha256(verificationToken), 
            Instant.now().plus(VERIFICATION_TOKEN_TTL)
        );

        userRepository.save(user);
        eventPublisher.publish(new UserRegisteredEvent(user.getId(), user.getRole(), Instant.now()));
        
        String verificationLink = publicApiUrl + "/api/v1/auth/verify-email?token=" + verificationToken;
        emailSender.sendEmailVerificationLink(user.getEmail(), verificationLink);
        
        return new RegisterResult(
            user.getId(),
            user.getEmail().value(),
            jwt.generateAccessToken(user.getId(), user.getRole()),
            jwt.generateRefreshToken(user.getId()),
            user.needsOnboarding()
        );
    }

    private String generateVerificationToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
