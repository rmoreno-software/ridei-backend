package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.RequestMotorbikePhotoUploadCommand;
import com.ridei.garage.domain.model.PresignedUpload;

public interface RequestMotorbikePhotoUploadUseCase {
    PresignedUpload request(RequestMotorbikePhotoUploadCommand command);
}
