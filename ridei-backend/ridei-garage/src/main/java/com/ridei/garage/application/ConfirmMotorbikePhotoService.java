package com.ridei.garage.application;

import com.ridei.garage.domain.exception.InvalidMotorbikePhotoUrlException;
import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.exception.MotorbikePhotoTooLargeException;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.port.in.ConfirmMotorbikePhotoUseCase;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class ConfirmMotorbikePhotoService implements ConfirmMotorbikePhotoUseCase {

    private static final long MAX_PHOTO_BYTES = 5L * 1024 * 1024; //5 MB

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikePhotoStoragePort motorbikePhotoStorage;

    public ConfirmMotorbikePhotoService (
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort motorbikePhotoStorage
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.motorbikePhotoStorage = motorbikePhotoStorage;
    }

    @Override
    public Motorbike confirm(ConfirmMotorbikePhotoCommand command) {
        Motorbike motorbike = motorbikeRepository.findById(command.motorbikeId())
            .filter(m -> m.isOwnedBy(command.ownerId()))
            .orElseThrow(MotorbikeNotFoundException::new);
    
        if (!motorbikePhotoStorage.belongsToMotorbike(
            command.ownerId(),
            command.motorbikeId(),
            command.publicUrl())) {
            throw new InvalidMotorbikePhotoUrlException();
        }

        if (motorbikePhotoStorage.getContentLength(command.publicUrl()) > MAX_PHOTO_BYTES) {
            motorbikePhotoStorage.delete(command.publicUrl());
            throw new MotorbikePhotoTooLargeException();
        }

        motorbike.attachPhoto(command.publicUrl());
        motorbikeRepository.save(motorbike);

        return motorbike;
    }
    
}
