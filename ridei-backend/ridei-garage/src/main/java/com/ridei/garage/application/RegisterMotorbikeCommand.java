package com.ridei.garage.application;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.OwnerId;

public record RegisterMotorbikeCommand(
    OwnerId ownerId,
    String brand,
    String model,
    Category category,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate
) {}
