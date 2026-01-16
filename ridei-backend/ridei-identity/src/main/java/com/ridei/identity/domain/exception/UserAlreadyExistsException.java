package com.ridei.identity.domain.exception;

/**
 * Domain-specific exception thrown to indicate violation of the user uniqueness business rule.
 * <p>
 * This exception is typically raised by the Application Service when attempting to register
 * a new user with an email address that is already persisted in the system.
 * </p>
 */
public class UserAlreadyExistsException extends RuntimeException{

    /**
     * Constructs a new exception with a detailed message identifying the conflicting entity.
     * 
     * @param email The email address that caused the uniqueness constraint violation
     */
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists");
    }
    
}
