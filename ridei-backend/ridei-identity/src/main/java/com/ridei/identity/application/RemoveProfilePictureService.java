package com.ridei.identity.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.RemoveProfilePictureUseCase;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class RemoveProfilePictureService implements RemoveProfilePictureUseCase {

    private final UserRepositoryPort userRepository;
    private final ProfilePictureStoragePort storage;

    public RemoveProfilePictureService(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        this.userRepository = userRepository;
        this.storage = storage;
    }

    @Override
    @Transactional
    public void remove(RemoveProfilePictureCommand command) {
        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        
            String currentPictureUrl = user.getPictureUrl();

            user.removePictureUrl();
            userRepository.update(user);

            if (currentPictureUrl != null) {
                storage.delete(currentPictureUrl);
            }
    }
}
