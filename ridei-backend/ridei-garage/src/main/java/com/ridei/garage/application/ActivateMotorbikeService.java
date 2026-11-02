package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.application.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.service.MotorbikeActivationPolicy;

public class ActivateMotorbikeService implements ActivateMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public ActivateMotorbikeService(MotorbikeRepositoryPort motorbikeRepository) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    @Transactional
    public Motorbike activate(OwnerId ownerId, MotorbikeId motorbikeId) {
        Motorbike motorbike = OwnedMotorbikes.require(motorbikeRepository, ownerId, motorbikeId);
        motorbike.activate();

        MotorbikeActivationPolicy.deactivateConflictsWith(motorbike, motorbikeRepository.findAllByOwnerId(ownerId))
            .forEach(motorbikeRepository::save);

        motorbikeRepository.save(motorbike);
        return motorbike;
    }
}
