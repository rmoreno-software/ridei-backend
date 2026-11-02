package com.ridei.identity.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.SaveOnboardingStep5UseCase;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class SaveOnboardingStep5Service implements SaveOnboardingStep5UseCase {

    private final UserRepositoryPort repositoryPort;

    public SaveOnboardingStep5Service(UserRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    @Transactional
    public void complete(SaveOnboardingStep5Command command) {
        User user = repositoryPort.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.acceptTerms();

        repositoryPort.update(user);
    }
    
}
