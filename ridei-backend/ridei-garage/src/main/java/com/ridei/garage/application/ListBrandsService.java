package com.ridei.garage.application;

import java.util.Comparator;
import java.util.List;

import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandUsage;
import com.ridei.garage.domain.port.in.ListBrandsUseCase;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;

public class ListBrandsService implements ListBrandsUseCase {

    private final BrandRepositoryPort brandRepository;

    public ListBrandsService(BrandRepositoryPort brandRepository) {
        this.brandRepository = brandRepository;
    }

    @Override
    public List<BrandUsage> listAll() {
        return brandRepository.findAllWithMotorbikeCount();
    }
}
