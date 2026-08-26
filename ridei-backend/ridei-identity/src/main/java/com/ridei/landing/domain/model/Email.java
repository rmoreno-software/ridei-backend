package com.ridei.landing.domain.model;

public record Email(String value) {
    public Email {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Email cannot be empty");
        if (!isValid(value))
            throw new IllegalArgumentException("Invalid email: " + value);
    }

    private static boolean isValid(String email) {
        if (email.contains(" ")) return false;
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) return false;
        int dotIndex = email.lastIndexOf('.');
        if (dotIndex <= atIndex + 1) return false;
        if (dotIndex >= email.length() - 1) return false;
        return true;
    }
}
