package com.ridei.identity.domain.port.out;

/**
 * Output Port (Driven Port) interface for secure password handling.
 * <p>
 * This interface abstracts the underlying cyptographic implementation (e.g., BCrypt, Argon2)
 * from the domain layer. By defining this contract within the domain, we adhere to the
 * <b>Dependency inversion Principle</b>, allowing the business logic to remain agnostic
 * of the specific security libraries or frameworks used in the infrastructure.
 * </p>
 */
public interface PasswordEncoder {

    /**
     * Performs a secure, one-way hashsing operation on the provided plain-text password.
     * <p>
     * <b>Security Requirement:</b> Implementation must ensure that the output is
     * resistant to reverse engineering (e.g., via Rainbow Tables) by utilizing
     * strong hashing algorithms and automatic salting.
     * </p>
     * 
     * @param rawPassword The plain-text password provided by the user.
     * @return The hashed (encoded) string suitable for safe storage in the database.
     */
    String encode(String rawPassword);
}
