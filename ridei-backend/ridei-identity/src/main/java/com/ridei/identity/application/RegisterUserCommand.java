package com.ridei.identity.application;

import java.time.Instant;
import java.time.LocalDate;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.Gender;
import com.ridei.identity.domain.model.IdentityDocument;
import com.ridei.identity.domain.model.PhoneNumber;
import com.ridei.identity.domain.model.UserRole;
import com.ridei.identity.domain.model.Username;

public record RegisterUserCommand (
    Email email,
    String password
) {}
