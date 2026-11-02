package com.ridei.garage.application.port.in;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.model.OwnerId;

public interface GetMotorbikeSetupUseCase {
    MotorbikeSetup get(OwnerId ownerId, MotorbikeId motorbikeId);
}
