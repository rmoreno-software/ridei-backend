package com.ridei.identity.domain.exception;

public class InvalidOrExpiredVerificationTokenException extends RuntimeException {
    public InvalidOrExpiredVerificationTokenException() {
        super("Invalid or expired verification token");
    }
}
