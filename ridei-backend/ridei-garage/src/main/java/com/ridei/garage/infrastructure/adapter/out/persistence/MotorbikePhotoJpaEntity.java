package com.ridei.garage.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import com.ridei.garage.domain.model.FocalPoint;
import com.ridei.garage.domain.model.MotorbikePhoto;
import com.ridei.garage.domain.model.PhotoId;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "motorbike_photos") 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class MotorbikePhotoJpaEntity {
    
    @Id 
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motorbike_id", nullable = false, updatable = false)
    private MotorbikeJpaEntity motorbike;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "focal_x", nullable = false)
    private double focalX;

    @Column(name = "focal_y", nullable = false)
    private double focalY;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    public static MotorbikePhotoJpaEntity  fromDomain(
        MotorbikePhoto photo,
        MotorbikeJpaEntity motorbikeEntity
    ) {
        return MotorbikePhotoJpaEntity .builder()
            .id(photo.getId().value())
            .motorbike(motorbikeEntity)
            .url(photo.getUrl())
            .focalX(photo.getFocalPoint().x())
            .focalY(photo.getFocalPoint().y())
            .primary(photo.isPrimary())
            .uploadedAt(photo.getUploadedAt())
            .build();
    }

    public MotorbikePhoto toDomain() {
        return MotorbikePhoto.reconstitute(
            new PhotoId(id), 
            url, 
            new FocalPoint(focalX, focalY), 
            primary, 
            uploadedAt
        );
    }
}
