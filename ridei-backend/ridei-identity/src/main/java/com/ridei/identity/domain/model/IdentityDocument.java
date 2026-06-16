package com.ridei.identity.domain.model;

public record IdentityDocument(DocumentType type, String number) {
    public IdentityDocument {
        if (type == null) throw new IllegalArgumentException("Document type is required");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("Document number is required");
    }
}
