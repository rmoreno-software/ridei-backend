package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.OwnerId;
import com.ridei.garage.domain.port.out.MotorbikeRepositoryPort;

import lombok.AllArgsConstructor;

@Component 
@AllArgsConstructor 
public class MotorbikePersistenceAdapter implements MotorbikeRepositoryPort {
    
    private final MotorbikeJpaRepository jpaRepository;
    
    @Override
    public void save(Motorbike motorbike) {
        jpaRepository.save(MotorbikeJpaEntity.fromDomain(motorbike));
    }

    @Override
    public List<Motorbike> findAllByOwnerId(OwnerId ownerId) {
        return jpaRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId.value()).stream()
            .map(MotorbikeJpaEntity::toDomain)
            .toList();
    }
    
}
