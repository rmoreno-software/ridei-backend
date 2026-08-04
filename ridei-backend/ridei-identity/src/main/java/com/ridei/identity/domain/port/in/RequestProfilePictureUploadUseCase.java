package com.ridei.identity.domain.port.in;

import com.ridei.identity.application.RequestProfilePictureUploadCommand;
import com.ridei.identity.domain.model.PresignedUpload;

public interface RequestProfilePictureUploadUseCase {
    PresignedUpload request(RequestProfilePictureUploadCommand command);
}
