package com.ridei.identity.domain.port.out;

public interface PasswordEncoder {
    String encode(String rawPassword);
}
