package com.ridei.identity.domain.model;

public record IdentityDocument(DocumentType type, String number) {
    public IdentityDocument {
        if (type == null) throw new IllegalArgumentException("error.document_type_required");
        if (number == null || number.isBlank()) throw new IllegalArgumentException("error.document_number_required");
    }
}
