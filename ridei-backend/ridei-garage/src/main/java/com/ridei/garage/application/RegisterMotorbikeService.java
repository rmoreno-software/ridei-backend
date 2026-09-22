package com.ridei.garage.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.service.MotorbikeActivationPolicy;

public class RegisterMotorbikeService implements  RegisterMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public RegisterMotorbikeService(MotorbikeRepositoryPort motorbikeRepository) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    @Transactional 
    public Motorbike register(RegisterMotorbikeCommand command) {
        Motorbike motorbike = Motorbike.register(
            command.ownerId(),
            command.brand(),
            command.model(),
            command.category(),
            command.year(),
            command.displacementCc(),
            command.weightKg(),
            command.acquisitionDate(),
            command.active()
        );

        if (motorbike.isActive()) {
            List<Motorbike> siblings = motorbikeRepository.findAllByOwnerId(command.ownerId());
            MotorbikeActivationPolicy.deactivateConflictsWith(motorbike, siblings)
                .forEach(motorbikeRepository::save);
        }

        motorbikeRepository.save(motorbike);

        return motorbike;
    }
}
