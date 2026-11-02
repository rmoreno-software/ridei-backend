package com.ridei.identity.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.identity.domain.exception.InvalidProfilePictureUrlException;
import com.ridei.identity.domain.exception.ProfilePictureTooLargeException;
import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.User;
import com.ridei.identity.application.port.in.ConfirmProfilePictureUseCase;
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
    @Transactional
    public void confirm(ConfirmProfilePictureCommand command) {
        if (!storage.belongsToUser(command.userId(), command.publicUrl())) {
            throw new InvalidProfilePictureUrlException(command.publicUrl());
        }

        User user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));

        user.updateProfilePicture(ensureWithinSizeLimit(command));
        userRepository.update(user);
    }

    /**
     * Devuelve la URL definitiva de la foto: la original si cabe en el límite, o la de una
     * versión recomprimida (borrando la original) si no. Si ni comprimida cabe, la borra y falla.
     */
    private String ensureWithinSizeLimit(ConfirmProfilePictureCommand command) {
        String originalUrl = command.publicUrl();
        if (storage.getContentLength(originalUrl) <= MAX_PROFILE_PICTURE_BYTES) {
            return originalUrl;
        }

        byte[] compressed = imageProcessor.compressToFit(storage.download(originalUrl), MAX_PROFILE_PICTURE_BYTES);
        if (compressed.length > MAX_PROFILE_PICTURE_BYTES) {
            storage.delete(originalUrl);
            throw new ProfilePictureTooLargeException();
        }

        String compressedUrl = storage.uploadProcessed(command.userId(), compressed, "jpg", "image/jpeg");
        storage.delete(originalUrl);
        return compressedUrl;
    }
}
