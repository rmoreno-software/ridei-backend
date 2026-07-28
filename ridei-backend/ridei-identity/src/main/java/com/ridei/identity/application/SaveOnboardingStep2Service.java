package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.SaveOnboardingStep2UseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class SaveOnboardingStep2Service implements SaveOnboardingStep2UseCase {

    private final UserRepositoryPort repository;

    public SaveOnboardingStep2Service(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void complete(SaveOnboardingStep2Command command) {
        // 1. Recuperar el usuario
        User user = repository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        
        // 2. Delegar la lógica al agregado
        user.saveOnboardingStep2(
            command.dateOfBirth(),
            command.countryCode(),
            command.phoneNumber()
        );

        repository.update(user);
    }
}
