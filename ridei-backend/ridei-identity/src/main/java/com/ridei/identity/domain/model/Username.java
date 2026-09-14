package com.ridei.identity.domain.model;

public record Username(String value) {
    public Username {
        if (value == null || !value.matches("^@[a-zA-Z0-9_.]{4,19}$"))
            throw new IllegalArgumentException("error.invalid_username");
    }
}
