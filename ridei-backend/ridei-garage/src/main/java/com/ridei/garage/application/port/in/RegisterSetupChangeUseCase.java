package com.ridei.garage.application.port.in;

import com.ridei.garage.application.RegisterSetupChangeCommand;
import com.ridei.garage.domain.model.MotorbikeSetup;

public interface RegisterSetupChangeUseCase {
    MotorbikeSetup register(RegisterSetupChangeCommand command);
}
