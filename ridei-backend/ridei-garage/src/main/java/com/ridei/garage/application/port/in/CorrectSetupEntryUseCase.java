package com.ridei.garage.application.port.in;

import com.ridei.garage.application.CorrectSetupEntryCommand;
import com.ridei.garage.domain.model.MotorbikeSetup;

public interface CorrectSetupEntryUseCase {
    MotorbikeSetup correct(CorrectSetupEntryCommand command);
}
