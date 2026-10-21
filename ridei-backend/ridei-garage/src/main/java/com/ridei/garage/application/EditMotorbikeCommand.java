package com.ridei.garage.application;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.ridei.garage.domain.model.BrandId;
import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

public record EditMotorbikeCommand(
    OwnerId ownerId,
    MotorbikeId motorbikeId,
    BrandId brandId,
    String customBrandName,
    String model,
    Category category,
    int year,
    Integer displacementCc,
    BigDecimal weightKg,
    LocalDate acquisitionDate,
    LocalDate disposalDate
) {}
