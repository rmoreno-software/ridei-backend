package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ConfirmProfilePictureUseCase;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ConfirmProfilePictureService implements ConfirmProfilePictureUseCase {

    private final UserRepositoryPort userRepository;
    private final ProfilePictureStoragePort storage;

    public ConfirmProfilePictureService(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        this.userRepository = userRepository;
        this.storage = storage;
    }

    @Override
    public void confirm(ConfirmProfilePictureCommand command) {
        if (!storage.belongsToUser(command.userId(), command.publicUrl())) {
            throw new InvalidProfilePictureUrlException(command.publicUrl());
        }

        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.updateProfilePicture(command.publicUrl());

        userRepository.update(user);
    }
    
}
