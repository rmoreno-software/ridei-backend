package com.ridei.identity.application;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.RequestTemporaryPasswordUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class RequestTemporaryPasswordService implements RequestTemporaryPasswordUseCase {

    private static final Duration TTL = Duration.ofMinutes(15);
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LENGTH = 12;

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordHasherPort passwordHasherPort;
    private final EmailSenderPort emailSenderPort;
    private final SecureRandom random = new SecureRandom();

    public RequestTemporaryPasswordService(
        UserRepositoryPort userRepositoryPort,
        PasswordHasherPort passwordHasher,
        EmailSenderPort emailSender
    ) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordHasherPort = passwordHasher;
        this.emailSenderPort = emailSender;
    }

    @Override
    public void request(RequestTemporaryPasswordCommand command) {
        parseEmail(command.email())
            .flatMap(userRepositoryPort::findByEmail)
            .ifPresent(this::issueAndSend);
    }

    private void issueAndSend(User user) {
        String temporaryPassword = generateTemporaryPassword();

        user.issueTemporaryPassword(passwordHasherPort.hash(temporaryPassword), Instant.now().plus(TTL));
        userRepositoryPort.save(user);

        emailSenderPort.sendTemporaryPassword(user.getEmail(), temporaryPassword);
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    private Optional<Email> parseEmail(String rawEmail) {
        try {
            return Optional.of(new Email(rawEmail));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
