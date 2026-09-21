package com.ridei.garage.application;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ridei.garage.domain.model.OwnerId;

public record RegisterMotorbikeCommand(
    OwnerId ownerId,
    String brand,
    String model,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate
) {}
