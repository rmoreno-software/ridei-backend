package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.exception.UsernameAlreadyTakenException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.SaveOnboardingStep1UseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class SaveOnboardingStep1Service implements SaveOnboardingStep1UseCase {

    private final UserRepositoryPort repository;

    public SaveOnboardingStep1Service(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void complete(SaveOnboardingStep1Command command) {
        // 1. Recuperar el usuario
        User user = repository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 2. Validar unicidad del username si ha cambiado
        if (user.getUsername() == null ||
                !user.getUsername().value().equals(command.username().value())) {
            if (repository.existsByUsername(command.username()))
                throw new UsernameAlreadyTakenException(command.username());
        }

        // 3. Delegar la lógica al agregado
        user.saveOnboardingStep1(
            command.firstName(),
            command.lastName(),
            command.username(),
            command.gender()
        );

        repository.update(user);
    }
}
