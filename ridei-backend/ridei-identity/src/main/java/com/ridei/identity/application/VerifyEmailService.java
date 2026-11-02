package com.ridei.identity.application;

import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.exception.InvalidOrExpiredVerificationTokenException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.VerifyEmailUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class VerifyEmailService implements VerifyEmailUseCase {

    private final UserRepositoryPort userRepository;

    public VerifyEmailService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void verify(VerifyEmailCommand command) {
        User user = findByValidToken(command.token())
            .orElseThrow(InvalidOrExpiredVerificationTokenException::new);

        user.verifyEmail();
        userRepository.update(user);
    }

    @Override
    public boolean isTokenValid(String token) {
        return findByValidToken(token).isPresent();
    }

    private Optional<User> findByValidToken(String rawToken) {
        return userRepository.findByEmailVerificationTokenHash(TokenHasher.sha256(rawToken))
            .filter(User::hasValidEmailVerificationToken);
    }
    
}
