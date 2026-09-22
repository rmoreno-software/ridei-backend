package com.ridei.garage.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

public interface MotorbikeRepositoryPort {
    void save(Motorbike motorbike);
    List<Motorbike> findAllByOwnerId(OwnerId ownerId);
    Optional<Motorbike> findById(MotorbikeId id);
}
