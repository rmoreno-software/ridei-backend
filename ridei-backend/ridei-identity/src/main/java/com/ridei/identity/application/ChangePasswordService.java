package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ChangePasswordUseCase;
import com.ridei.identity.domain.port.out.EmailSenderPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;
    private final EmailSenderPort emailSender;
    private final JwtPort jwt;

    public ChangePasswordService(
        UserRepositoryPort userRepository,
        PasswordHasherPort passwordHasher,
        EmailSenderPort emailSender,
        JwtPort jwt
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.emailSender = emailSender;
        this.jwt = jwt;
    }

    @Override
    public ChangePasswordResult change(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        
        user.changePassword(passwordHasher.hash(command.newPassword()));

        userRepository.update(user);

        emailSender.sendPasswordChangedNotification(user.getEmail(), command.locale());

        return new ChangePasswordResult(
            jwt.generateAccessToken(user.getId(), user.getRole(), user.getTokenVersion()),
            jwt.generateRefreshToken(user.getId(), user.getTokenVersion())
        );
    }
}
