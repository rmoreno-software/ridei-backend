package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.Brand;

public record BrandResponseDTO(
    String id,
    String brand
) {
    public static BrandResponseDTO fromDomain(Brand brand) {
        return new BrandResponseDTO(
            brand.getId().value().toString(), 
            brand.getName()
        );
    }
}
