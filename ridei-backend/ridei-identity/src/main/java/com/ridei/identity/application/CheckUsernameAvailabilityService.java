package com.ridei.identity.application;

import com.ridei.identity.domain.model.Username;
import com.ridei.identity.domain.model.UsernameAvailabilityReason;
import com.ridei.identity.domain.port.in.CheckUsernameAvailabilityUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CheckUsernameAvailabilityService implements CheckUsernameAvailabilityUseCase {

    private final UserRepositoryPort repository;

    @Override
    public UsernameAvailabilityResult check(String rawUsername) {
        if (rawUsername == null || rawUsername.isBlank())
            return UsernameAvailabilityResult.unavailable(UsernameAvailabilityReason.INVALID_FORMAT);

        Username username;
        try {
            username = new Username("@" + rawUsername);
        } catch (IllegalArgumentException e) {
            return UsernameAvailabilityResult.unavailable(UsernameAvailabilityReason.INVALID_FORMAT);
        }

        if (repository.existsByUsername(username))
            return UsernameAvailabilityResult.unavailable(UsernameAvailabilityReason.ALREADY_TAKEN);

        return UsernameAvailabilityResult.AVAILABLE;
    }

}
