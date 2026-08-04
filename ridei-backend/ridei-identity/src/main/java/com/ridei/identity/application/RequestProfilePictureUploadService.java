package com.ridei.identity.application;

import com.ridei.identity.domain.exception.UserNotFoundException;
import com.ridei.identity.domain.model.PresignedUpload;
import com.ridei.identity.domain.port.in.RequestProfilePictureUploadUseCase;
import com.ridei.identity.domain.port.out.ProfilePictureStoragePort;
import com.ridei.identity.domain.port.out.UserRepositoryPort;

public class RequestProfilePictureUploadService implements RequestProfilePictureUploadUseCase {

    private final UserRepositoryPort userRepository;
    private final ProfilePictureStoragePort storage;

    public RequestProfilePictureUploadService(
        UserRepositoryPort userRepository,
        ProfilePictureStoragePort storage
    ) {
        this.userRepository = userRepository;
        this.storage = storage;
    }

    @Override
    public PresignedUpload request(RequestProfilePictureUploadCommand command) {
        userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException(command.userId()));
        
        return storage.createUploadUrl(command.userId(), command.contentType());
    }
    
}
