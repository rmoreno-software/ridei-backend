package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.EditMotorbikeCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface EditMotorbikeUseCase {
    Motorbike edit(EditMotorbikeCommand command);
}
