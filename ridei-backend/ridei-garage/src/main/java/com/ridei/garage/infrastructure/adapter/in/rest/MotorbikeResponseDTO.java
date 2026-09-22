package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.ridei.garage.domain.model.Motorbike;

public record MotorbikeResponseDTO(
    String id,
    String brand,
    String model,
    String category,
    String categoryGroup,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate,
    LocalDate disposalDate,
    boolean active,
    String photoUrl,
    Instant createdAt
) {
    public static MotorbikeResponseDTO fromDomain(Motorbike motorbike) {
        return new MotorbikeResponseDTO(
            motorbike.getId().value().toString(),
            motorbike.getBrand(),
            motorbike.getModel(),
            motorbike.getCategory().name(),
            motorbike.getCategoryGroup().name(),
            motorbike.getYear(),
            motorbike.getDisplacementCc(),
            motorbike.getWeightKg(),
            motorbike.getAcquisitionDate(),
            motorbike.getDisposalDate(),
            motorbike.isActive(),
            motorbike.getPhotoUrl(),
            motorbike.getCreatedAt()
        );
    }
}
