package com.ridei.identity.infrastructure.adapter.in.rest;

import com.ridei.identity.domain.model.DocumentType;

final class DocumentTypeCodec {

    private DocumentTypeCodec() {}

    static DocumentType decode(String code) {
        if (code == null) throw new IllegalArgumentException("Document type is required");
        return switch (code) {
            case "dni" -> DocumentType.NATIONAL_ID;
            case "passport" -> DocumentType.PASSPORT;
            case "residencePermit" -> DocumentType.RESIDENCE_PERMIT;
            case "drivingLicense" -> DocumentType.DRIVER_LICENSE;
            default -> throw new IllegalArgumentException("Unknown document type: " + code);
        };
    }
    
}
