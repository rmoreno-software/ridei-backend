package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.application.port.in.CorrectSetupEntryUseCase;
import com.ridei.garage.domain.model.MotorbikeSetup;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.port.out.MotorbikeSetupRepositoryPort;

public class CorrectSetupEntryService implements CorrectSetupEntryUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikeSetupRepositoryPort setupRepository;

    public CorrectSetupEntryService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikeSetupRepositoryPort setupRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.setupRepository = setupRepository;
    }

    @Override
    @Transactional
    public MotorbikeSetup correct(CorrectSetupEntryCommand command) {
        OwnedMotorbikes.require(motorbikeRepository, command.ownerId(), command.motorbikeId());

        MotorbikeSetup setup = setupRepository.findByMotorbikeId(command.motorbikeId());
        setup.correct(command.entryId(), command.spec(), command.mountedOn());

        setupRepository.save(setup);
        return setup;
    }
}
