package com.ridei.garage.application;

import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

final class OwnedMotorbikes {
    private OwnedMotorbikes() {}

    static Motorbike require(
        MotorbikeRepositoryPort repository,
        OwnerId ownerId,
        MotorbikeId motorbikeId
    ) {
        return repository.findById(motorbikeId)
            .filter(m -> m.isOwnedBy(ownerId))
            .orElseThrow(MotorbikeNotFoundException::new);
    }
}
