package com.ridei.garage.application;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikePhoto;
import com.ridei.garage.domain.port.in.DeleteMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class DeleteMotorbikePhotoService implements  DeleteMotorbikePhotoUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikePhotoStoragePort motorbikePhotoStorage;

    public DeleteMotorbikePhotoService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort motorbikePhotoStorage
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.motorbikePhotoStorage = motorbikePhotoStorage;
    }

    @Override
    @Transactional 
    public Motorbike delete(DeleteMotorbikePhotoCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(
            motorbikeRepository, 
            command.ownerId(), 
            command.motorbikeId()
        );

        MotorbikePhoto removed = motorbike.removePhoto(command.photoId());
        motorbikeRepository.save(motorbike);
        motorbikePhotoStorage.delete(removed.getUrl());

        return motorbike;
    }
    
}
