package com.ridei.identity.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.ResetPasswordUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ResetPasswordService implements ResetPasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final EmailSenderPort emailSender;

    public ResetPasswordService(
        UserRepositoryPort userRepository,
        PasswordHasherPort passwordHasher,
        EmailSenderPort emailSender
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
    }

    @Override
    @Transactional
    public void reset(ResetPasswordCommand command) {
        User user = userRepository.findByEmail(command.email())
            .filter(u -> isValidTemporaryPassword(u, command.temporaryPassword()))
            .orElseThrow(InvalidCredentialException::new);

        user.changePassword(passwordHasher.hash(command.newPassword()));
        userRepository.update(user);
        emailSender.sendPasswordChangedNotification(user.getEmail(), command.locale());
    }

    private boolean isValidTemporaryPassword(User user, String candidate) {
        return user.hasValidTemporaryPassword()
            && passwordHasher.matches(candidate, user.getTemporaryPasswordHash());
    }
}
