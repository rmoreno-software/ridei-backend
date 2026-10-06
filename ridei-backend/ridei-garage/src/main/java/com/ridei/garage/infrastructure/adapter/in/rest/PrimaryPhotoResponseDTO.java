package com.ridei.garage.infrastructure.adapter.in.rest;

import com.ridei.garage.domain.model.MotorbikePhoto;

public record PrimaryPhotoResponseDTO(
    String id,
    String url,
    double focalX,
    double focalY
) {
    public static PrimaryPhotoResponseDTO fromDomain(MotorbikePhoto photo) {
        return new PrimaryPhotoResponseDTO(
            photo.getId().value().toString(),
            photo.getUrl(),
            photo.getFocalPoint().x(),
            photo.getFocalPoint().y()
        );
    }
}
