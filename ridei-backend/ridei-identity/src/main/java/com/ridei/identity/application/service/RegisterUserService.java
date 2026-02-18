package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.RegisterUserCommand;
import com.ridei.identity.application.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.exception.UserEmailAlreadyExistsException;
import com.ridei.identity.domain.exception.UserNicknameAlreadyExistsException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.out.PasswordEncoder;
import com.ridei.identity.domain.port.out.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Application Service implementation for the User Registration Use Case.
 * <p>
 * This class acts as the orchestrator of the business logic. It sits between the
 * Input Port (UseCase interface) and the Output Port (Repository/Encoder).
 * It is responsible for encoding high-level business rules that depend on the
 * system state (e.g., uniqueness checks) and coordinating the transaction.
 * </p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase{
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation Details:</b>
     * <ol>
     * <li>Checks if the email is already taken by querying the repository (Stateful Validation).</li>
     * <li>Delegates password hashing to the infrastructure adapter.</li>
     * <li>Instantiates the {@link User} Aggregate Root using the factory method.</li>
     * <li>Persists the new entity to the database.</li>
     * </ol>
     * </p>
     */
    @Override
    public void register(RegisterUserCommand command) {
        // 1. Business Rule: Email Uniqueness (Stateful check)
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            log.warn("Registration attempt failed: Email {} already exists", command.getEmail());
            throw new UserEmailAlreadyExistsException(command.getEmail());
        }

        // 2. Business Rule: Nickname Uniqueness (Stateful check)
        if (userRepository.findByNickname(command.getNickname()).isPresent()) {
            log.warn("Registration attempt failed: Nickname {} already exists", command.getEmail());
            throw new UserNicknameAlreadyExistsException(command.getNickname());
        }

        // 2. Security: Hash the password
        String encodedPassword = passwordEncoder.encode(command.getPassword());

        // 3. Domain Logic: Create the Aggregate Root
        User newUser = User.create(
            command.getEmail(),
            command.getNickname(),
            encodedPassword,
            command.getName()
        );

        // 4. Persistence: Save state
        userRepository.save(newUser);

        log.info("User successfully registered with ID: {}", newUser.getId());
    }

    
}
