package com.ridei.identity.application;

import java.util.Optional;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ResendVerificationEmailUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ResendVerificationEmailService implements  ResendVerificationEmailUseCase {

    private final UserRepositoryPort userRepository;
    private final EmailSenderPort emailSender;
    private final String publicApiUrl;
    private final EmailVerificationTokenFactory tokenFactory = new EmailVerificationTokenFactory();

    public ResendVerificationEmailService(
        UserRepositoryPort userRepository,
        EmailSenderPort emailSender,
        String publicApiUrl
    ) {
        this.userRepository = userRepository;
        this.emailSender = emailSender;
        this.publicApiUrl = publicApiUrl;
    }

    @Override
    public void resend(ResendVerificationEmailCommand command) {
        userRepository.findByEmail(command.email())
            .filter(user -> user.getStatus() == AccountStatus.PENDING_VERIFICATION)
            .ifPresent(this::issueAndSend);
    }

    private void issueAndSend(User user) {
        EmailVerificationTokenFactory.IssuedToken token = tokenFactory.issue();

        user.issueEmailVerificationToken(token.hashToken(), token.expiresAt());
        userRepository.update(user);

        String verificationLink = tokenFactory.buildVerificationLink(publicApiUrl, token.rawToken());
        emailSender.sendEmailVerificationLink(user.getEmail(), verificationLink);
    }
}
