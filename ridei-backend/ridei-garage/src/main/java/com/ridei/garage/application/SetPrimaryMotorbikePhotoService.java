package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.SetPrimaryMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class SetPrimaryMotorbikePhotoService implements SetPrimaryMotorbikePhotoUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public SetPrimaryMotorbikePhotoService(
        MotorbikeRepositoryPort motorbikeRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    @Transactional 
    public Motorbike setPrimary(SetPrimaryMotorbikePhotoCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(
            motorbikeRepository, 
            command.ownerId(), 
            command.motorbikeId()
        );
        motorbike.setPrimaryPhoto(command.photoId());
        motorbikeRepository.save(motorbike);
        return motorbike;
    }
    
}
