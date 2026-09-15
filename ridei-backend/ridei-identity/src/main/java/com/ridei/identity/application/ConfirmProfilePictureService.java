package com.ridei.identity.application;

import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.ProfilePictureTooLargeException;
import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.domain.port.in.ConfirmProfilePictureUseCase;
import com.ridei.identity.domain.port.out.ImageProcessorPort;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class ConfirmProfilePictureService implements ConfirmProfilePictureUseCase {

    private static final long MAX_PROFILE_PICTURE_BYTES = 5L * 1024 * 1024; // 5 MB
    
    private final UserRepositoryPort userRepository;
    private final ProfilePictureStoragePort storage;
    private final ImageProcessorPort imageProcessor;

    public ConfirmProfilePictureService(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage,
        ImageProcessorPort imageProcessor
    ) {
        this.userRepository = userRepository;
        this.storage = storage;
        this.imageProcessor = imageProcessor;
    }

    @Override
    public void confirm(ConfirmProfilePictureCommand command) {
        if (!storage.belongsToUser(command.userId(), command.publicUrl())) {
            throw new InvalidProfilePictureUrlException(command.publicUrl());
        }

        String finalUrl = command.publicUrl();

        if (storage.getContentLength(finalUrl) > MAX_PROFILE_PICTURE_BYTES) {
            byte[] original = storage.download(finalUrl);
            byte[] compressed = imageProcessor.compressToFit(original, MAX_PROFILE_PICTURE_BYTES);

            if (compressed.length > MAX_PROFILE_PICTURE_BYTES) {
                storage.delete(finalUrl);
                throw new ProfilePictureTooLargeException();
            }

            finalUrl = storage.uploadProcessed(command.userId(), compressed, "jpg", "image/jpeg");
            storage.delete(command.publicUrl());

            User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

            user.updateProfilePicture(finalUrl);

            userRepository.update(user);
        }
    }
    
}
