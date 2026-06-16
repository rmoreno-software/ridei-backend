package com.ridei.identity.infrastructure.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.port.out.PasswordHasherPort;

@Component
public class BCryptPasswordHasherAdapter implements PasswordHasherPort{

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(); 

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

}
