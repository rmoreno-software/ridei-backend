package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.UpdateMotorbikePhotoFocalPointUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class UpdateMotorbikePhotoFocalPointService implements UpdateMotorbikePhotoFocalPointUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public UpdateMotorbikePhotoFocalPointService(
        MotorbikeRepositoryPort motorbikeRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
    }
    
    @Override
    @Transactional 
    public Motorbike updateFocalPoint(UpdateMotorbikePhotoFocalPointCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(
            motorbikeRepository, 
            command.ownerId(), 
            command.motorbikeId()
        );
        motorbike.updatePhotoFocalPoint(
            command.photoId(), 
            command.focalPoint()
        );
        motorbikeRepository.save(motorbike);
        return motorbike;
    }
    
}
