package com.ridei.identity.application.port.in;

/**
 * Input Port interface defining the contract for the User Registration Use Case.
 * <p>
 * This interface serves as the entry point to the application core. It decouples the
 * triggering infrastructure (e.g, {@code AuthController}) from the business logic implementation.
 * Primary adapters invoke this interface to initiate the registration process.
 * </p>
 */
public interface RegisterUserUseCase {

    /**
     * Executes the business logic to register a new user in the system.
     * <p>
     * This method orchestrates the necessary steps such as:
     * <ul>
     * <li>Verifying if the user already exists (Business Rule).</li>
     * <li>Hashing the password securely.</li>
     * <li>Persisting the new user entity.</li>
     * </ul>
     * </p>
     * 
     * @param command The immutable command object containing the validated user data
     * (email, password, name). Must not be {@code null}.
     */
    void register(RegisterUserCommand command);
}
