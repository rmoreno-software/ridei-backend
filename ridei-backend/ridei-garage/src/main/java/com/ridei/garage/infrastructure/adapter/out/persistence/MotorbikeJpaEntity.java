package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.ridei.garage.domain.model.Category;
import com.ridei.garage.domain.model.Motorbike;
import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.OwnerId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "motorbikes")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder  
public class MotorbikeJpaEntity {
    
    @Id
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 100)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(nullable = false)
    private int year;

    @Column(name = "displacement_cc")
    private Integer displacementCc;

    @Column(name = "weight_kg", precision = 6, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "acquisition_date")
    private LocalDate acquisitionDate;

    @Column(name = "disposal_date")
    private LocalDate disposalDate;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static MotorbikeJpaEntity fromDomain(Motorbike motorbike) {
        return MotorbikeJpaEntity.builder()
            .id(motorbike.getId().value())
            .ownerId(motorbike.getOwnerId().value())
            .brand(motorbike.getBrand())
            .model(motorbike.getModel())
            .category(motorbike.getCategory())
            .year(motorbike.getYear())
            .displacementCc(motorbike.getDisplacementCc())
            .weightKg(motorbike.getWeightKg())
            .acquisitionDate(motorbike.getAcquisitionDate())
            .disposalDate(motorbike.getDisposalDate())
            .photoUrl(motorbike.getPhotoUrl())
            .createdAt(motorbike.getCreatedAt())
            .build();
    }

    public Motorbike toDomain() {
        return Motorbike.reconstitute(
            new MotorbikeId(id),
            new OwnerId(ownerId),
            brand,
            model,
            category,
            year,
            displacementCc,
            weightKg,
            acquisitionDate,
            disposalDate,
            photoUrl,
            createdAt
        );
    }
    
}
