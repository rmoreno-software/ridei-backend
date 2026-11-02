package com.ridei.garage.domain.port.in;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

public interface DeactivateMotorbikeUseCase {
    Motorbike deactivate(OwnerId ownerId, MotorbikeId motorbikeId);
}
