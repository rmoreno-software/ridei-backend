package com.ridei.identity.domain.exception;

public class MinimumAgeNotMetException extends RuntimeException {
    public MinimumAgeNotMetException() {
        super("error.minimum_age_not_met");
    }
}
