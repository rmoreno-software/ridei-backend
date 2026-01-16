package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.RegisterUserCommand;
import com.ridei.identity.application.port.in.RegisterUserUseCase;
import com.ridei.identity.domain.exception.UserAlreadyExistsException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.out.PasswordEncoder;
import com.ridei.identity.domain.port.out.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RegisterUserService implements RegisterUserUseCase{
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException(command.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(command.getPassword());

        User newUser = User.create(
            command.getEmail(),
            encodedPassword,
            command.getName()
        );

        userRepository.save(newUser);

        System.out.println("User successfully registered: " + newUser.getId());
    }

    
}
