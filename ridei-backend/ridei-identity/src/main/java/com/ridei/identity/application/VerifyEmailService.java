package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidOrExpiredVerificationTokenException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.VerifyEmailUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class VerifyEmailService implements VerifyEmailUseCase {

    private final UserRepositoryPort userRepository;

    public VerifyEmailService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void verify(VerifyEmailCommand command) {
        String tokenHash = TokenHasher.sha256(command.token());

        User user = userRepository.findByEmailVerificationTokenHash(tokenHash)
            .filter(User::hasValidEmailVerificationToken)
            .orElseThrow(InvalidOrExpiredVerificationTokenException::new);
        
        user.verifyEmail();

        userRepository.update(user);
    }
    
}
