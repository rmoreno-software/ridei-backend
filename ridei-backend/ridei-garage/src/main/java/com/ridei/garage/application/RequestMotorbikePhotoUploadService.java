package com.ridei.garage.application;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.PresignedUpload;
import com.ridei.garage.application.port.in.RequestMotorbikePhotoUploadUseCase;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class RequestMotorbikePhotoUploadService implements RequestMotorbikePhotoUploadUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikePhotoStoragePort motorbikePhotoStorage;

    public RequestMotorbikePhotoUploadService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort motorbikePhotoStorage
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.motorbikePhotoStorage = motorbikePhotoStorage;
    }

    @Override
    public PresignedUpload request(RequestMotorbikePhotoUploadCommand command) {
        Motorbike motorbike = OwnedMotorbikes.require(motorbikeRepository, command.ownerId(), command.motorbikeId());
        return motorbikePhotoStorage.createUploadUrl(motorbike.getOwnerId(), motorbike.getId(), command.contentType());
    }
}
