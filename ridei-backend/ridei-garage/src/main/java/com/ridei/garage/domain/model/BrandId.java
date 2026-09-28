package com.ridei.garage.domain.model;

import java.util.UUID;

public record BrandId(UUID value) {
    public BrandId {
        if (value == null) {
            if (value == null) throw new IllegalArgumentException("Brand id is required");
        }
    }
}
