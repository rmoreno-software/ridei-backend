package com.ridei.identity.application;

import com.ridei.identity.domain.model.Email;
import com.ridei.identity.domain.model.EmailAvailabilityReason;
import com.ridei.identity.domain.port.in.CheckEmailAvailabilityUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CheckEmailAvailabilityService implements CheckEmailAvailabilityUseCase {

    private final UserRepositoryPort repository;

    @Override
    public EmailAvailabilityResult check(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            return EmailAvailabilityResult.unavailable(EmailAvailabilityReason.INVALID_FORMAT);
        }

        Email email;
        try {
            email = new Email(rawEmail);
        } catch (IllegalArgumentException e) {
            return EmailAvailabilityResult.unavailable(EmailAvailabilityReason.INVALID_FORMAT);
        }

        if (repository.existsByEmail(email))
            return EmailAvailabilityResult.unavailable(EmailAvailabilityReason.ALREADY_TAKEN);

        return EmailAvailabilityResult.AVAILABLE;
    }
    
}
