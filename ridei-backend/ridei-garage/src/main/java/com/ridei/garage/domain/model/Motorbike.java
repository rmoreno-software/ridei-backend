package com.ridei.garage.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.ridei.garage.domain.exception.CannotActivateRetiredMotorbikeException;
import com.ridei.garage.domain.exception.PhotoNotFoundException;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
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
    private final List<MotorbikePhoto> photos;
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
            new ArrayList<>(),
            Instant.now()
        );

        if (active) {
            motorbike.activate();
        }

        return motorbike;
    }

    public static Motorbike reconstitute(
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
        List<MotorbikePhoto> photos,
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
            new ArrayList<>(photos),
            createdAt
        );
    }

    // ---- Fotos ---------------------------------------------------------

    public void addPhoto(String url, FocalPoint focalPoint) {
        boolean alreadyAttached = photos.stream().anyMatch(p -> p.getUrl().equals(url));
        if (alreadyAttached)
            throw new IllegalArgumentException("Photo already attached to this motorbike");

        photos.add(MotorbikePhoto.register(url, focalPoint, photos.isEmpty()));
    }

    public void setPrimaryPhoto(PhotoId photoId) {
        MotorbikePhoto target = findPhoto(photoId);
        photos.forEach(MotorbikePhoto::unmarkAsPrimary);
        target.markAsPrimary();
    }

    public void updatePhotoFocalPoint(PhotoId photoId, FocalPoint focalPoint) {
        findPhoto(photoId).changeFocalPoint(focalPoint);
    }

    /**
     * Quita la foto del agregado. Si era la principal y quedan más, promociona la más reciente.
     * @return la foto eliminada, para que la capa de aplicación borre también el objeto en storage
     */
    public MotorbikePhoto removePhoto(PhotoId photoId) {
        MotorbikePhoto removed = findPhoto(photoId);
        photos.remove(removed);

        if (removed.isPrimary()) {
            photos.stream()
                .max(Comparator.comparing(MotorbikePhoto::getUploadedAt))
                .ifPresent(MotorbikePhoto::markAsPrimary);
        }
        return removed;
    }

    public Optional<MotorbikePhoto> primaryPhoto() {
        return photos.stream().filter(MotorbikePhoto::isPrimary).findFirst();
    }

    public List<MotorbikePhoto> getPhotos() {
        return List.copyOf(photos);
    }

    private MotorbikePhoto findPhoto(PhotoId photoId) {
        return photos.stream()
            .filter(p -> p.getId().equals(photoId))
            .findFirst()
            .orElseThrow(PhotoNotFoundException::new);
    }

    // ---- Activación / baja ---------------------------------------------

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

    public boolean isVerifiedBrand() {
        return brandId != null;
    }

    public CategoryGroup getCategoryGroup() {
        return this.category.group();
    }

    // ---- Validaciones ----------------------------------------------------

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException(field + " is required");
    }

    private static void requireValidDisposalDate(LocalDate acquisitionDate, LocalDate disposalDate) {
        if (disposalDate == null) return;

        if (disposalDate.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Disposal date cannot be in the future");
        if (acquisitionDate != null && disposalDate.isBefore(acquisitionDate))
            throw new IllegalArgumentException("Disposal date cannot be before acquisition date");
    }
}
