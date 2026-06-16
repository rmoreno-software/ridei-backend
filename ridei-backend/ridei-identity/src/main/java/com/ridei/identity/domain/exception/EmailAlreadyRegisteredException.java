package com.ridei.identity.domain.exception;

import com.ridei.identity.domain.model.Email;

public class EmailAlreadyRegisteredException extends RuntimeException{
    public EmailAlreadyRegisteredException(Email email) {
        super("Email already registered: " + email.value());
    }
}
