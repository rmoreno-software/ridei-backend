package com.ridei.garage.domain.model;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter 
public class MotorbikePhoto {
    
    private final PhotoId id;
    private final String url;
    private FocalPoint focalPoint;
    private boolean primary;
    private final Instant uploadedAt;

    static MotorbikePhoto register(
        String url,
        FocalPoint focalPoint,
        boolean primary
    ) {
        if (url == null || url.isBlank())
            throw new IllegalArgumentException("Photo URL is required");
        return new MotorbikePhoto(
            PhotoId.newId(),
            url,
            focalPoint != null ? focalPoint : FocalPoint.CENTER,
            primary,
            Instant.now()
        );
    }

    public static MotorbikePhoto reconstitute(
        PhotoId id,
        String url,
        FocalPoint focalPoint,
        boolean primary,
        Instant uploadedAt
    ) {
        return new MotorbikePhoto(id, url, focalPoint, primary, uploadedAt);
    }

    void markAsPrimary() {
        this.primary = true;
    }

    void unmarkAsPrimary() {
        this.primary = false;
    }

    void changeFocalPoint(FocalPoint focalPoint) {
        this.focalPoint = focalPoint;
    }
}
