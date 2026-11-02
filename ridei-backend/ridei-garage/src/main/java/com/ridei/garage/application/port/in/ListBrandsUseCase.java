package com.ridei.garage.domain.port.in;

import java.util.List;

import com.ridei.garage.domain.model.BrandUsage;

public interface ListBrandsUseCase {
    List<BrandUsage> listAll();
}
