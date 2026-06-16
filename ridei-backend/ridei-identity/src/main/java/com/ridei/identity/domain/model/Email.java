package com.ridei.identity.domain.model;

public record Email(String value) {
    public Email {
        if (value == null || !value.matches("^[^@]+@[^@]+\\\\.[^@]+$"))
            throw new IllegalArgumentException("Invalid email: " + value);
    }
}
