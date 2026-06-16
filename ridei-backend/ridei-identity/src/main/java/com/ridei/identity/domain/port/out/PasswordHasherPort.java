package com.ridei.identity.domain.port.out;

public interface PasswordHasherPort {
    String hash(String rawPassword);
}
