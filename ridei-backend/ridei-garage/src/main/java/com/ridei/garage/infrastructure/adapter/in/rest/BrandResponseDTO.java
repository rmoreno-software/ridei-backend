package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.BrandUsage;

public record BrandResponseDTO(
    String id,
    String brand,
    long motorbikeCount
) {
    public static BrandResponseDTO fromDomain(BrandUsage usage) {
        return new BrandResponseDTO(
            usage.brand().getId().value().toString(), 
            usage.brand().getName(),
            usage.motorbikeCount()
        );
    }
}
