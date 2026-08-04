package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.model.ImageContentType;
import com.ridei.identity.domain.model.PresignedUpload;
import com.ridei.identity.domain.model.UserId;

public interface ProfilePictureStoragePort {
    PresignedUpload createUploadUrl(UserId userId, ImageContentType contentType);
}
