package com.ridei.identity.domain.model;

public record PhoneNumber(String value) {
    public PhoneNumber {
        if (value == null || !value.matches("^\\+?[1-9]\\d{6,14}$"))
            throw new IllegalArgumentException("Invalid phone number: " + value);
    }
}
