package com.ridei.identity.application.service;

import org.springframework.stereotype.Service;

import com.ridei.identity.application.port.in.RegisterUserUseCase;
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
    public void register(String email, String password, String name) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("The user with email " + email + " already exists");
        }

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = User.create(email, encodedPassword, name);

        userRepository.save(newUser);

        System.out.println("User successfully registered: " + newUser.getId());
    }

    
}
