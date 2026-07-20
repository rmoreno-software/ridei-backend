package com.ridei.identity.domain.model;

public record Username(String value) {
    public Username {
        if (value == null || !value.matches("^@[a-zA-Z0-9_.]{4,19}$"))
            throw new IllegalArgumentException(
                "Invalid username. Must start with @ and contain 4-19 alphanumeric characters, with no spaces.");
    }
}
