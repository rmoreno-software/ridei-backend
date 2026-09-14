package com.ridei.identity.domain.exception;

public class InvalidCredentialException extends RuntimeException {
    public InvalidCredentialException() {
        super("error.invalid_credential");
    }
}
