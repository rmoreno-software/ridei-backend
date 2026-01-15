package com.ridei.identity.infrastructure.adapter.out.security;

import java.util.Base64;

import org.springframework.stereotype.Component;

import com.ridei.identity.domain.port.out.PasswordEncoder;

@Component
public class SimplePasswordEncoder implements PasswordEncoder{

    @Override
    public String encode(String rawPassword) {
        // POR AHORA: Solo codificamos en Base64 para probar. 
        // Más adelante pondremos BCrypt real.
        return Base64.getEncoder().encodeToString(rawPassword.getBytes());
    }
    
}
