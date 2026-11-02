package com.ridei.garage.domain.model;

import java.util.UUID;

public record SetupEntryId(UUID value) {
    public SetupEntryId {
        if (value == null) throw new IllegalArgumentException("Setup entry id is required");
    }

    public static SetupEntryId newId() {
        return new SetupEntryId(UUID.randomUUID());
    }

    public static SetupEntryId of(String value) {
        return new SetupEntryId(UUID.fromString(value));
    }
}
