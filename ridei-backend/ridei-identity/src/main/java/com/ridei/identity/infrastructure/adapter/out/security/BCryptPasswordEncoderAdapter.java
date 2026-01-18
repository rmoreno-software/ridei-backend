package com.ridei.identity.infrastructure.adapter.out.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.port.out.PasswordEncoder;

/**
 * Secondary Adapter (Driven Adapter) implementing secure password hashing.
 * <p>
 * This class provides the concrete implementation of the {@link PasswordEncoder} output port
 * using the <b>BCrypt</b> strong hashing function.
 * </p>
 * <p>
 * <b>Security Mechanism:</b> It delegates the cryptographic operations to Spring Security's
 * {@link BCryptPasswordEncoder}. This algorithm is chosen for its resistance to
 * brute-force attacks via a configurable work factor (computational cost).
 * </p>
 */
@Component
public class BCryptPasswordEncoderAdapter implements PasswordEncoder{

    /**
     * The underlying Spring Security implementation.
     * <p>
     * Initialized with default strength (10), which provides a balanced trade-off
     * between security and performance for standard web applications.
     * </p>
     */
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * {@inheritDoc}
     * <p>
     * <b>Implementation Details:</b>
     * <ul>
     * <li>Generates a random <b>salt</b> internally for every execution.</li>
     * <li>Hashes the password combined with the salt.</li>
     * <li>Returns a standard BCrypt string format (e.g., {@code $2a$10$...}).</li>
     * </ul>
     * </p>
     *
     * @param rawPassword The plain-text password to protect.
     * @return The hashed string including version, strength, salt, and hash.
     */
    @Override
    public String encode(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

}
