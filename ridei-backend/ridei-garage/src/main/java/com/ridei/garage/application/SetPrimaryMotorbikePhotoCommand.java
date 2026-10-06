package com.ridei.garage.application;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.model.PhotoId;

public record SetPrimaryMotorbikePhotoCommand (
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    PhotoId photoId
) {}
