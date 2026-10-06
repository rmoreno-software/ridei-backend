package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.ridei.garage.domain.model.Motorbike;

public record MotorbikeResponseDTO(
    String id,
    String brandId,
    String brandName,
    boolean verifiedBrand,
    String model,
    String category,
    String categoryGroup,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate,
    LocalDate disposalDate,
    boolean active,
    PrimaryPhotoResponseDTO primaryPhoto,
    Instant createdAt
) {
    public static MotorbikeResponseDTO fromDomain(Motorbike motorbike) {
        return new MotorbikeResponseDTO(
            motorbike.getId().value().toString(),
            motorbike.isVerifiedBrand() ? motorbike.getBrandId().value().toString() : null,
            motorbike.getBrandName(),
            motorbike.isVerifiedBrand(),
            motorbike.getModel(),
            motorbike.getCategory().name(),
            motorbike.getCategoryGroup().name(),
            motorbike.getYear(),
            motorbike.getDisplacementCc(),
            motorbike.getWeightKg(),
            motorbike.getAcquisitionDate(),
            motorbike.getDisposalDate(),
            motorbike.isActive(),
            motorbike.primaryPhoto().map(PrimaryPhotoResponseDTO::fromDomain).orElse(null),
            motorbike.getCreatedAt()
        );
    }
}
