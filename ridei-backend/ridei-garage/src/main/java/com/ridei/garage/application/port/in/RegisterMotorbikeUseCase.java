package com.ridei.garage.domain.port.in;

import com.ridei.garage.application.RegisterMotorbikeCommand;
import com.ridei.garage.domain.model.Motorbike;

public interface RegisterMotorbikeUseCase {
    Motorbike register(RegisterMotorbikeCommand command);
}
