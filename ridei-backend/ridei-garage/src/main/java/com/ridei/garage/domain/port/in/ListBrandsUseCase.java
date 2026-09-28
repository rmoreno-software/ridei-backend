package com.ridei.garage.domain.port.in;

import java.util.List;

import com.ridei.garage.domain.model.Brand;

public interface ListBrandsUseCase {
    List<Brand> listAll();
}
