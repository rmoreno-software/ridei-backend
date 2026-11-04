package com.ridei.circuits.domain.exception;

import java.util.UUID;

/** Se intenta modificar un elemento ya fusionado; las operaciones deben hacerse sobre el canónico. */
public class MergedCatalogItemException extends RuntimeException {

    private final UUID mergedInto;

    public MergedCatalogItemException(String kind, UUID id, UUID mergedInto) {
        super(kind + " " + id + " was merged into " + mergedInto);
        this.mergedInto = mergedInto;
    }

    public UUID getMergedInto() {
        return mergedInto;
    }
}
