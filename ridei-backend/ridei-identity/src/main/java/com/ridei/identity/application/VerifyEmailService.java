package com.ridei.identity.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ridei.identity.domain.exception.InvalidOrExpiredVerificationTokenException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.VerifyEmailUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class VerifyEmailService implements VerifyEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(VerifyEmailService.class);

    private final UserRepositoryPort userRepository;

    public VerifyEmailService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void verify(VerifyEmailCommand command) {
        String tokenHash = TokenHasher.sha256(command.token());
        log.debug("VerifyEmail: looking up token hash {}", tokenHash);

        User user = userRepository.findByEmailVerificationTokenHash(tokenHash)
            .filter(u -> {
                boolean valid = u.hasValidEmailVerificationToken();
                log.debug("VerifyEmail: user {} found, hasValidEmailVerificationToken={}", u.getId().value(), valid);
                return valid;
            })
            .orElseThrow(() -> {
                log.debug("VerifyEmail: no user found for that token hash (already consumed, or never existed)");
                return new InvalidOrExpiredVerificationTokenException();
            });
        
        user.verifyEmail();
        userRepository.update(user);
        log.debug("VerifyEmail: user {} verified successfully, status updated", user.getId().value());
    }
    
}
