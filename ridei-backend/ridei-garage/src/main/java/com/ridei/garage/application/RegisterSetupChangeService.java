package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.application.port.in.RegisterSetupChangeUseCase;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

public class RegisterSetupChangeService implements RegisterSetupChangeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikeSetupRepositoryPort setupRepository;

    public RegisterSetupChangeService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.setupRepository = setupRepository;
    }

    @Override
    @Transactional
    public MotorbikeSetup register(RegisterSetupChangeCommand command) {
        OwnedMotorbikes.require(motorbikeRepository, command.ownerId(), command.motorbikeId());

        MotorbikeSetup setup = setupRepository.findByMotorbikeId(command.motorbikeId());
        command.changes().forEach((component, spec) -> setup.mount(component, spec, command.mountedOn()));

        setupRepository.save(setup);
        return setup;
    }
}
