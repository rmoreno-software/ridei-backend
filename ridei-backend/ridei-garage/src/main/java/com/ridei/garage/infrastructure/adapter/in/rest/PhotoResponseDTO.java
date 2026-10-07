package com.ridei.garage.infrastructure.adapter.in.rest;

import java.time.Instant;

import com.ridei.garage.domain.model.MotorbikePhoto;

public record PhotoResponseDTO(
    String id,
    String url,
    double focalX,
    double focalY,
    boolean primary,
    Instant uploadedAt
) {
    public static PhotoResponseDTO fromDomain(MotorbikePhoto photo) {
        return new PhotoResponseDTO(
            photo.getId().value().toString(), 
            photo.getUrl(), 
            photo.getFocalPoint().x(), 
            photo.getFocalPoint().y(), 
            photo.isPrimary(), 
            photo.getUploadedAt()
        );
    }
}
