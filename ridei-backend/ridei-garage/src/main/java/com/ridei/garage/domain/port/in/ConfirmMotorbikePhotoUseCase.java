package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.ConfirmMotorbikePhotoCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface ConfirmMotorbikePhotoUseCase {
    Motorbike confirm(ConfirmMotorbikePhotoCommand command);
}
