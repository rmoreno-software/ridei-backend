package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;

import lombok.AllArgsConstructor;

@Component 
@AllArgsConstructor 
public class BrandPersistenceAdapter implements BrandRepositoryPort {
    
    private final BrandJpaRepository jpaRepository;
    
    @Override
    public List<Brand> findAll() {
        return jpaRepository.findAll().stream()
            .map(BrandJpaEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<Brand> findById(BrandId id) {
        return jpaRepository.findById(id.value())
            .map(BrandJpaEntity::toDomain);
    }
    
}
