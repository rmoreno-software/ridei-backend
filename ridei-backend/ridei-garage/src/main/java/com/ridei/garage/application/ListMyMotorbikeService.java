package com.ridei.garage.application;

import java.util.List;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.in.ListMyMotorbikesUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

public class ListMyMotorbikeService implements ListMyMotorbikesUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public ListMyMotorbikeService(MotorbikeRepositoryPort motorbikeRepository) {
        this.motorbikeRepository = motorbikeRepository;
    }

    @Override
    public List<Motorbike> list(OwnerId ownerId) {
        return motorbikeRepository.findAllByOwnerId(ownerId);
    }

}
