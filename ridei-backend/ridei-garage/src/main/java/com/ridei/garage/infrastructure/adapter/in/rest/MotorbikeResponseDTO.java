package com.ridei.garage.infrastructure.adapter.in.rest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.ridei.garage.domain.model.Motorbike;

public record MotorbikeResponseDTO(
    String id,
    String brand,
    String model,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate,
    LocalDate disposalDate,
    String photoUrl,
    Instant createdAt
) {
    public static MotorbikeResponseDTO fromDomain(Motorbike motorbike) {
        return new MotorbikeResponseDTO(
            motorbike.getId().value().toString(),
            motorbike.getBrand(),
            motorbike.getModel(),
            motorbike.getYear(),
            motorbike.getDisplacementCc(),
            motorbike.getWeightKg(),
            motorbike.getAcquisitionDate(),
            motorbike.getDisposalDate(),
            motorbike.getPhotoUrl(),
            motorbike.getCreatedAt()
        );
    }
}
