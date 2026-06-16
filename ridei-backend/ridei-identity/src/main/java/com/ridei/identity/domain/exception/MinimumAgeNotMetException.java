package com.ridei.identity.domain.exception;

public class MinimumAgeNotMetException extends RuntimeException {
    public MinimumAgeNotMetException() {
        super("User must be at least 16 years old");
    }
}
