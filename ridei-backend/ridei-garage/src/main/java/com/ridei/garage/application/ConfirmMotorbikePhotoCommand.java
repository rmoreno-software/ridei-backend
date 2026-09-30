package com.ridei.garage.application;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

public record ConfirmMotorbikePhotoCommand(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    String publicUrl
) {}
