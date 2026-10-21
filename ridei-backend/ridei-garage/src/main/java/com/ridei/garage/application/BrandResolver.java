package com.ridei.garage.application;

import com.ridei.garage.domain.exception.BrandNotFoundException;
import com.ridei.garage.domain.model.Brand;
import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.port.out.BrandRepositoryPort;

final class BrandResolver {
    private BrandResolver() {}

    /**
     * Si llega brandId, resuelve el nombre canónico desde el catálogo (ignora cualquier otro
     * dato que el cliente mandara). Si no, usa el texto libre tal cual.
     */
    static Resolved resolve(BrandRepositoryPort brandRepository, BrandId brandId, String customBrandName) {
        if (brandId == null) {
            return new Resolved(null, customBrandName);
        }
        Brand brand = brandRepository.findById(brandId).orElseThrow(BrandNotFoundException::new);
        return new Resolved(brand.getId(), brand.getName());
    }

    record Resolved(BrandId brandId, String brandName) {}
}
