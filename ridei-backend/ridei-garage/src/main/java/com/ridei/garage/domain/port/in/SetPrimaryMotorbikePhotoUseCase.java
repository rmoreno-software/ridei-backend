package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.SetPrimaryMotorbikePhotoCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface SetPrimaryMotorbikePhotoUseCase {
    Motorbike setPrimary(SetPrimaryMotorbikePhotoCommand command);
}
