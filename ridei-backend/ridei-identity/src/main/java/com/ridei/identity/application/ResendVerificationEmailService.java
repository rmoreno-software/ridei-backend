package com.ridei.identity.application;

import java.util.Locale;

import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ResendVerificationEmailUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ResendVerificationEmailService implements ResendVerificationEmailUseCase {

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
            .ifPresent(user -> issueAndSend(user, command.locale()));
    }

    private void issueAndSend(User user, Locale locale) {
        EmailVerificationTokenFactory.IssuedToken token = tokenFactory.issue();

        user.issueEmailVerificationToken(token.hashToken(), token.expiresAt());
        userRepository.update(user);

        String verificationLink = tokenFactory.buildVerificationLink(publicApiUrl, token.rawToken(), locale);
        emailSender.sendEmailVerificationLink(user.getEmail(), verificationLink, locale);
    }
}
