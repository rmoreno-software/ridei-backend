package com.ridei.identity.domain.exception;

/**
 * Domain-specific exception thrown to indicate violation of the user uniqueness business rule.
 * <p>
 * This exception is typically raised by the Application Service when attempting to register
 * a new user with an nickname that is already persisted in the system.
 * </p>
 */
public class UserNicknameAlreadyExistsException extends RuntimeException{

    /**
     * Constructs a new exception with a detailed message identifying the conflicting entity.
     * 
     * @param nickname The nickname that caused the uniqueness constraint violation
     */
    public UserNicknameAlreadyExistsException(String nickname) {
        super("User with nickname " + nickname + " already exists");
    }
    
}
