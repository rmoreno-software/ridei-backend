package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.application.port.in.DeactivateMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class DeactivateMotorbikeService implements DeactivateMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public DeactivateMotorbikeService(MotorbikeRepositoryPort motorbikeRepository) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    @Transactional
    public Motorbike deactivate(OwnerId ownerId, MotorbikeId motorbikeId) {
        Motorbike motorbike = OwnedMotorbikes.require(motorbikeRepository, ownerId, motorbikeId);
        motorbike.deactivate();
        motorbikeRepository.save(motorbike);
        return motorbike;
    }
}
