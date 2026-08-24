package com.ridei.identity.domain.exception;

public class InvalidCredentialException extends RuntimeException {
    public InvalidCredentialException() {
        super("Invalid email or Password");
    }
}
