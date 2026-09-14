package com.ridei.identity.domain.model;

public record Email(String value) {
    public Email {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("error.email_empty");
        if (!isValid(value))
            throw new IllegalArgumentException("error.email_invalid");
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
