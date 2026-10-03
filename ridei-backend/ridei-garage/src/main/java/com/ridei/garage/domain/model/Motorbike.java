package com.ridei.garage.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;

import com.ridei.garage.domain.exception.CannotActivateRetiredMotorbikeException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor (access = AccessLevel.PRIVATE)
@Getter 
public class Motorbike {
    
    private static final int FIRST_MOTORBIKE_YEAR = 1887;

    private final MotorbikeId id;
    private final OwnerId ownerId;
    private BrandId brandId;
    private String brandName;
    private String model;
    private Category category;
    private int year;
    private Integer displacementCc;
    private BigDecimal weightKg;
    private LocalDate acquisitionDate;
    private LocalDate disposalDate;
    private boolean active;
    private String photoUrl;
    private final Instant createdAt;

    public static Motorbike register(
        OwnerId ownerId,
        BrandId brandId,
        String brandName,
        String model,
        Category category,
        int year,
        Integer displacementCc,
        BigDecimal weightKg,
        LocalDate acquisitionDate,
        LocalDate disposalDate,
        boolean active
    ) {
        if (ownerId == null) throw new IllegalArgumentException("Owner is required");
        requireText(brandName, "Brand name");
        requireText(model, "Model");

        if (category == null) {
            throw new IllegalArgumentException("Category is required");
        }
        int maxYear = Year.now().getValue() + 1;
        if (year < FIRST_MOTORBIKE_YEAR || year > maxYear)
            throw new IllegalArgumentException("Year must be between + " + FIRST_MOTORBIKE_YEAR + " and " + maxYear);
        if (displacementCc != null && displacementCc <= 0)
            throw new IllegalArgumentException("Displacement must be positive");
        if (weightKg != null && weightKg.signum() <= 0)
            throw new IllegalArgumentException("Weight must be positive");
        if (acquisitionDate != null && acquisitionDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Acquisition  date cannot be in the future");
        requireValidDisposalDate(acquisitionDate, disposalDate);
        
        Motorbike motorbike = new Motorbike(
            MotorbikeId.newId(),
            ownerId,
            brandId,
            brandName.trim(),
            model.trim(),
            category,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            disposalDate,
            false,
            null,
            Instant.now()
        );

        if (active) {
            motorbike.activate();
        }

        return motorbike;
    }

    public static Motorbike reconstitute (
        MotorbikeId id,
        OwnerId ownerId,
        BrandId brandId,
        String brandName,
        String model,
        Category category,
        int year,
        Integer displacementCc,
        BigDecimal weightKg,
        LocalDate acquisitionDate,
        LocalDate disposalDate,
        boolean active,
        String photoUrl,
        Instant createdAt
    ) {
        if (active && disposalDate != null) {
            throw new CannotActivateRetiredMotorbikeException();
        }
        
        return new Motorbike(
            id,
            ownerId,
            brandId,
            brandName,
            model,
            category,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            disposalDate,
            active,
            photoUrl,
            createdAt
        );
    }

    public void activate() {
        if (isRetired()) {
            throw new CannotActivateRetiredMotorbikeException();
        }
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
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

    public boolean isVerifiedBrand() {
        return brandId != null;
    }

    public CategoryGroup getCategoryGroup() {
        return this.category.group();
    }

    public void attachPhoto(String photoUrl) {
        if (photoUrl== null || photoUrl.isBlank()) {
            throw new IllegalArgumentException("Photo URL is required");
        }
        this.photoUrl = photoUrl;
    }

    private static void requireValidDisposalDate(LocalDate acquisitionDate, LocalDate disposalDate) {
        if (disposalDate == null) return;

        if (disposalDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Disposal date cannot be in the future");
        if (acquisitionDate != null && disposalDate.isBefore(acquisitionDate))
            throw new IllegalArgumentException("Disposal date cannot be before acquisition date");
    }
}
