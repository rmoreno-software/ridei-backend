package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.exception.UserSuspendedException;
import com.ridei.identity.domain.model.AccountStatus;
import com.ridei.identity.domain.model.UserId;
import com.ridei.identity.domain.model.UserProfile;
import com.ridei.identity.domain.port.in.GetCurrentUserUseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GetCurrentUserService implements GetCurrentUserUseCase {

    private final UserRepositoryPort repository;

    @Override
    public UserProfile get(UserId userId) {
        UserProfile profile = repository.findByIdWithProfile(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        if (profile.status() == AccountStatus.SUSPENDED)
            throw new UserSuspendedException();

        return profile;
    }
    
}
