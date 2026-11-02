package com.ridei.garage.application;

import com.ridei.garage.application.port.in.GetMotorbikeSetupUseCase;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

public class GetMotorbikeSetupService implements GetMotorbikeSetupUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikeSetupRepositoryPort setupRepository;

    public GetMotorbikeSetupService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.setupRepository = setupRepository;
    }

    @Override
    public MotorbikeSetup get(OwnerId ownerId, MotorbikeId motorbikeId) {
        OwnedMotorbikes.require(motorbikeRepository, ownerId, motorbikeId);
        return setupRepository.findByMotorbikeId(motorbikeId);
    }
}
