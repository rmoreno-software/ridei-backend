package com.ridei.identity.domain.exception;

public class InvalidOrExpiredVerificationTokenException extends RuntimeException {
    public InvalidOrExpiredVerificationTokenException() {
        super("error.invalid_verification_token");
    }
}
