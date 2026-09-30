package com.ridei.garage.application;

import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.PresignedUpload;
import com.ridei.garage.domain.port.in.RequestMotorbikePhotoUploadUseCase;
import com.ridei.garage.domain.port.out.MotorbikePhotoStoragePort;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class RequestMotorbikePhotoUploadService implements RequestMotorbikePhotoUploadUseCase {
    
    private final MotorbikeRepositoryPort motorbikeRepository;
    private final MotorbikePhotoStoragePort motorbikePhotoStorag;

    public RequestMotorbikePhotoUploadService(
        MotorbikeRepositoryPort motorbikeRepository,
        MotorbikePhotoStoragePort motorbikePhotoStorage
    ) {
        this.motorbikeRepository = motorbikeRepository;
        this.motorbikePhotoStorag = motorbikePhotoStorage;
    }

    @Override
    public PresignedUpload request(RequestMotorbikePhotoUploadCommand command) {
        Motorbike motorbike = motorbikeRepository.findById(
            command.motorbikeId()
        ).filter(m -> m.isOwnedBy(command.ownerId()))
        .orElseThrow(MotorbikeNotFoundException::new);

        return motorbikePhotoStorag.createUploadUrl(
            motorbike.getOwnerId(),
            motorbike.getId(),
            command.contentType()
        );
    }
}
