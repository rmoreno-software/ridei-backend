package com.ridei.identity.application;

import java.time.Instant;

import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.exception.EmailAlreadyRegisteredException;
import com.ridei.identity.domain.exception.UsernameAlreadyTakenException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.port.out.EventPublisherPort;
import com.ridei.identity.domain.port.out.PasswordHasherPort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {
    
    private final UserRepositoryPort userRepository;
    private final EventPublisherPort eventPublisher;
    private final PasswordHasherPort passwordHasher;

    @Override
    public UserId register(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email()))
            throw new EmailAlreadyRegisteredException(command.email());

        if (userRepository.existsByUsername(command.username()))
            throw new UsernameAlreadyTakenException(command.username());

        User user = User.register(command, passwordHasher.hash(command.password()));
        userRepository.save(user);
        eventPublisher.publish(new UserRegisteredEvent(user.getId(), user.getRole(), Instant.now()));
        return user.getId();
    }

}
