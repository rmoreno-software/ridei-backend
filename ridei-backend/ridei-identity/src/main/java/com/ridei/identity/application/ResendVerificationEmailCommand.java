package com.ridei.identity.application;

import com.ridei.identity.domain.model.Email;

public record ResendVerificationEmailCommand(
    Email email
) {
    
}
