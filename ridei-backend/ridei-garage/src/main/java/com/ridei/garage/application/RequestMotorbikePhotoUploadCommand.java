package com.ridei.garage.application;

import com.ridei.garage.domain.model.ImageContentType;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

public record RequestMotorbikePhotoUploadCommand(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    ImageContentType contentType
) {}
