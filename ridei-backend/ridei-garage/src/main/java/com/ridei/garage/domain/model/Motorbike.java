package com.ridei.garage.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor (access = AccessLevel.PRIVATE)
@Getter 
public class Motorbike {
    
    private static final int FIRST_MOTORBIKE_YEAR = 1887;

    private final MotorbikeId id;
    private final OwnerId ownerId;
    private String brand;
    private String model;
    private int year;
    private Integer displacementCc;
    private BigDecimal weightKg;
    private LocalDate acquisitionDate;
    private LocalDate disposalDate;
    private String photoUrl;
    private final Instant createdAt;

    public static Motorbike register(
        OwnerId ownerId,
        String brand,
        String model,
        int year,
        Integer displacementCc,
        BigDecimal weightKg,
        LocalDate acquisitionDate
    ) {
        if (ownerId == null) throw new IllegalArgumentException("Owner is required");
        requireText(brand, "Brand");
        requireText(model, "Model");

        int maxYear = Year.now().getValue() + 1;
        if (year < FIRST_MOTORBIKE_YEAR || year > maxYear)
            throw new IllegalArgumentException("Year must be between + " + FIRST_MOTORBIKE_YEAR + " and " + maxYear);
        if (displacementCc != null && displacementCc <= 0)
            throw new IllegalArgumentException("Displacement must be positive");
        if (weightKg != null && weightKg.signum() <= 0)
            throw new IllegalArgumentException("Weight must be positive");
        if (acquisitionDate != null && acquisitionDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Acquisition  date cannot be in the future");
        
        return new Motorbike(
            MotorbikeId.newId(),
            ownerId,
            brand.trim(),
            model.trim(),
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            null,
            null,
            Instant.now()
        );
    }

    public static Motorbike reconstitute (
        MotorbikeId id,
        OwnerId ownerId,
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
        return new Motorbike(
            id,
            ownerId,
            brand,
            model,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            disposalDate,
            photoUrl,
            createdAt
        );
    }

    public boolean isRetired() {
        return disposalDate != null;
    }

    public boolean isOwnedBy(OwnerId candidate) {
        return ownerId.equals(candidate);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " is required");
    }
}
