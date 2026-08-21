package com.ridei.identity.application;

import java.time.Instant;

import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.model.RegisterResult;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.JwtPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {
    
    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordHasherPort passwordHasher;
    private final JwtPort jwt;

    @Override
    public RegisterResult register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyRegisteredException(command.email());

        User user = User.register(
            command.email(),
            passwordHasher.hash(command.password())
        );

        userRepository.save(user);
        eventPublisher.publish(new UserRegisteredEvent(user.getId(), user.getRole(), Instant.now()));
        
        return new RegisterResult(
            user.getId(),
            user.getEmail().value(),
            jwt.generateAccessToken(user.getId(), user.getRole()),
            jwt.generateRefreshToken(user.getId()),
            user.needsOnboarding()
        );
    }

}
