package com.ridei.garage.application;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.RegisterMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class RegisterMotorbikeService implements  RegisterMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public RegisterMotorbikeService(MotorbikeRepositoryPort motorbikeRepository) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    public Motorbike register(RegisterMotorbikeCommand command) {
        Motorbike motorbike = Motorbike.register(
            command.ownerId(),
            command.brand(),
            command.model(),
            command.category(),
            command.year(),
            command.displacementCc(),
            command.weightKg(),
            command.acquisitionDate()
        );

        motorbikeRepository.save(motorbike);

        return motorbike;
    }
}
