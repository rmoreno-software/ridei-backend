package com.ridei.sessions.domain.model;

import java.util.UUID;

public record LayoutId(UUID value) {
    public LayoutId {
        if (value == null) throw new IllegalArgumentException("Layout id is required");
    }

    public static LayoutId of(String value) {
        try {
            return new LayoutId(UUID.fromString(value));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Layout id must be a UUID");
        }
    }

    public static LayoutId of(UUID value) {
        return new LayoutId(value);
    }
}
