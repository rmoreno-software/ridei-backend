package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.UpdateMotorbikePhotoFocalPointCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface UpdateMotorbikePhotoFocalPointUseCase {
    Motorbike updateFocalPoint(UpdateMotorbikePhotoFocalPointCommand command);
}
