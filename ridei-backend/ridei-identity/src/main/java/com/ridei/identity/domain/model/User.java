package com.ridei.identity.domain.model;

import java.util.UUID;

import lombok.Getter;

/**
 * Represents the User **Aggregate Root** within the Identity Domain
 * <p>
 * This entity encapsule the core state and identity of a registered user.
 * It is distinct from Data Transfer Objects (DTOs) as it possesses a unique identifier
 * that persists throughout its lifecycle, regardless of state changes.
 * </p>
 * <p>
 * <b>Design Patter:</b> Uses a <i>Static Factory Method</i> and a private constructor
 * to enure valid objects creation and encapsule identity generation. 
 * </p>
 */
@Getter
public class User {

    /**
     * The unique immutable identifier of the user (Aggregate ID).
     */
    private final UUID id;

    /**
     * The user's email address.
     * Functions as a natural key or secondary index in the persistance layer.
     */
    private final String email;

    /**
     * The user's nickname.
     * Functions as a natural key or secondary index in the persistance layer.
     */
    private final String nickname;

    /**
     * The <b>securely hashed</b> password.
     * <p>
     * <b>Security Note:</b> This field must never contain plain-text passwords.
     * It stores the result of the {@link com.ridei.identity.domain.port.out.PasswordEncoder}.
     * </p>
     */
    private final String password;
    
    /**
     * The user's full name.
     */
    private final String name;

    /**
     * Private constuctor to prevent direct instantiation.
     * <p>
     * Enforces the use of the {@link #create(String, String, String)} factory method
     * ensuring that all instances are created with a valid generated ID.
     * </p>
     * 
     * @param id        The unique ID.
     * @param email     The email.
     * @param password  The hashed password.
     * @param name      The name.
     */
    private User(UUID id, String email, String nickname, String password, String name) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.name = name;
    }

    /**
     * Factory method to initialize a new User entity.
     * <p>
     * This method handles the generation of a new {@link UUID} for the entity.
     * It should be used when a user is being registered for the first time.
     * </p>
     * 
     * @param email     The validated email address.
     * @param password  The <b>already encoded</b> password string.
     * @param name      The user's name.
     * @return A new instance of {@link User} with a generated ID.
     */
    public static User create(String email, String nickname, String password, String name) {
        return new User(UUID.randomUUID(), email, nickname, password, name);
    }

    /**
     * Factory method for <b>reconstituting</b> an existing user from the database.
     * <p>
     * This method bypasses creation logic (like ID generation) and acts purely
     * to restore the state of an Aggregate Root that already exists in the system.
     * </p>
     *
     * @param id       The existing UUID from the database.
     * @param email    The stored email.
     * @param password The stored hash.
     * @param name     The stored name.
     * @return The reconstituted User object.
     */
    public static User restore(UUID id, String email, String nickname, String password, String name) {
        return new User(id, email, nickname, password, name);
    }
}
