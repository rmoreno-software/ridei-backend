package com.ridei.identity.application;

import java.util.Locale;

import com.ridei.identity.domain.model.Email;

public record RegisterUserCommand (
    Email email,
    String password,
    Locale locale
) {}
