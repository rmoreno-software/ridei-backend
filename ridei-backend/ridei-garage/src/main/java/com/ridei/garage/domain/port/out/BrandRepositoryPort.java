package com.ridei.garage.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.model.BrandUsage;

public interface BrandRepositoryPort {
    List<Brand> findAll();
    Optional<Brand> findById(BrandId id);
    List<BrandUsage> findAllWithMotorbikeCount();
}
