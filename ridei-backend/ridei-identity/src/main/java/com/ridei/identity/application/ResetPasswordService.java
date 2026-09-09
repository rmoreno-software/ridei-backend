package com.ridei.identity.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ridei.identity.domain.exception.InvalidCredentialException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ResetPasswordUseCase;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ResetPasswordService implements ResetPasswordUseCase {
    
    private static final Logger log = LoggerFactory.getLogger(ResetPasswordService.class);
    
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
            .orElseThrow(() -> {
                log.debug("Reset password failed: no user found for the given email");
                return new InvalidCredentialException();
            });
        
        log.debug("Reset password: user {} found, hasValidTemporaryPassword={}",
            user.getId().value(), user.hasValidTemporaryPassword());
        
        boolean temporaryPasswordValid = user.hasValidTemporaryPassword()
            && passwordHasher.matches(command.temporaryPassword(), user.getTemporaryPasswordHash());
        
        log.debug("Reset password: user {} temporaryPasswordValid={}", user.getId().value(), temporaryPasswordValid);

        if (!temporaryPasswordValid) {
            log.debug("Reset password failed: temporary password invalid or expired for user {}", user.getId().value());
            throw new InvalidCredentialException();
        }
            

        user.changePassword(passwordHasher.hash(command.newPassword()));

        userRepository.update(user);

        log.debug("Reset password: completed successfully for user {}", user.getId().value());
    }
}
