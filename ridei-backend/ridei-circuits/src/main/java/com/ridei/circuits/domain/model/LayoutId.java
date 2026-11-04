package com.ridei.circuits.domain.model;

import java.util.UUID;

public record LayoutId(UUID value) {

    public LayoutId {
        if (value == null) throw new IllegalArgumentException("Layout id is required");
    }

    public static LayoutId newId() {
        return new LayoutId(UUID.randomUUID());
    }

    public static LayoutId of(String value) {
        return new LayoutId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
