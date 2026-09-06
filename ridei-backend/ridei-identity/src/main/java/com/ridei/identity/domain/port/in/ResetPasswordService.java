package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.ResetPasswordCommand;
import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ResetPasswordService implements ResetPasswordUseCase {
    
    private final UserRepositoryPort userRepository;
    private final PasswordHasherPort passwordHasher;

    public ResetPasswordService(
        UserRepositoryPort userRepository,
        PasswordHasherPort passwordHasher
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void reset(ResetPasswordCommand command) {
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(InvalidCredentialException::new);
    
        boolean temporaryPasswordValid = user.hasValidTemporaryPassword()
            && passwordHasher.matches(command.temporaryPassword(), user.getTemporaryPasswordHash());
        
        if (!temporaryPasswordValid)
            throw new InvalidCredentialException();

        user.changePassword(passwordHasher.hash(command.newPassword()));

        userRepository.update(user);
    }
}
