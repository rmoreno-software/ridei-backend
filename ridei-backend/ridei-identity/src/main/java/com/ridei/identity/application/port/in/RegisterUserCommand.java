package com.ridei.identity.application.port.in;

import com.ridei.identity.shared.SelfValidating;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Represents the immutable intent to register a new user in the system.
 * <p>
 * This class serves as a DTO (Data Transfer Object) for the Input Port Layer.
 * It extends {@link SelfValidating} to ensure domain data integrity upon instantation.
 * If the provider data violates the defined constraints, the object creation will fail,
 * preventing invalid state from entering the application core.
 * </p>
 */
@Getter
public class RegisterUserCommand extends SelfValidating<RegisterUserCommand> {

    /**
     * The user's email address.
     * Acts as the unique identifier for the user account. Must not be blank.
     */
    @NotBlank(message = "The email address is invalid")
    private final String email;

    /**
     * The raw password provided by the user.
     * <p>
     * <b>Security Note:</b> This field holds the plain-text password before hashing.
     * It enforces a minimum length policy to ensure basic security strength.
     * </p>
     */
    @NotBlank(message = "The password is required")
    @Size(min = 6, message = "The password must be at least 6 characters long")
    private final String password;

    /**
     * The user's full name or display name
     */
    @NotBlank(message = "The name is required")
    private final String name;

    /**
     * Constructs a new command and triggers the self-validation mechanism.
     * 
     * @param email     The user's email address (cannot be null or empty)
     * @param password  The user's raw password (must meet length requirements)
     * @param name      The user's name
     * @throws jakarta.validation.ConstraintViolationException if any field violates constraints.
     */
    public RegisterUserCommand(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;

        // Triggers the Jakarta Been Validation defined by the annotations above.
        this.validateSelf();
    }

}
