package com.ridei.garage.application;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.ridei.garage.domain.exception.MotorbikeNotFoundException;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.in.ActivateMotorbikeUseCase;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;
import com.ridei.garage.domain.service.MotorbikeActivationPolicy;

public class ActivateMotorbikeService implements ActivateMotorbikeUseCase {

    private final MotorbikeRepositoryPort motorbikeRepository;

    public ActivateMotorbikeService(
        MotorbikeRepositoryPort motorbikeRepository
    ) {
        this.motorbikeRepository = motorbikeRepository;
    }
    
    @Override
    @Transactional 
    public Motorbike activate(OwnerId ownerId, MotorbikeId motorbikeId) {
        Motorbike motorbike = motorbikeRepository.findById(motorbikeId)
            .filter(m -> m.isOwnedBy(ownerId))
            .orElseThrow(MotorbikeNotFoundException::new);
        
            motorbike.activate();

            List<Motorbike> siblings = motorbikeRepository.findAllByOwnerId(ownerId);
            MotorbikeActivationPolicy.deactivateConflictsWith(motorbike, siblings)
                .forEach(motorbikeRepository::save);
            
            motorbikeRepository.save(motorbike);

            return motorbike;
    }
    
}
