package com.ridei.identity.domain.model;

import java.util.Set;

public record ImageContentType(String value) {
     private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");

     public ImageContentType {
        if (value == null || !ALLOWED.contains(value)) {
            throw new IllegalArgumentException("Unsupported image content type: " + value);
        }
     }

     public String extension() {
        return switch (value) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalStateException("Unreachable");
        };
     }
}
