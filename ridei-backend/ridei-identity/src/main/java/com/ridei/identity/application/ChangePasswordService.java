package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ChangePasswordUseCase;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ChangePasswordService implements ChangePasswordUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public ChangePasswordService(
        UserRepositoryPort userRepository,
        PasswordHasherPort passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void change(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        
        user.changePassword(passwordHasher.hash(command.newPassword()));

        userRepository.update(user);
    }
}
