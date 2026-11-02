package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.DeleteMotorbikePhotoCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface DeleteMotorbikePhotoUseCase {
    Motorbike delete(DeleteMotorbikePhotoCommand command);
}
