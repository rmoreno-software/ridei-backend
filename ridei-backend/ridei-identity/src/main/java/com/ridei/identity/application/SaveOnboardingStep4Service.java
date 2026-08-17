package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.SaveOnboardingStep4UseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class SaveOnboardingStep4Service implements SaveOnboardingStep4UseCase {

    private final UserRepositoryPort repository;

    public SaveOnboardingStep4Service(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void complete(SaveOnboardingStep4Command command) {
        User user = repository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.saveOnboardingStep4(command.identityDocument());

        repository.update(user);
    }
    
}
