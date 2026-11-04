package com.ridei.circuits.domain.model;

/**
 * Lo que propone un usuario entra como {@code PENDING_REVIEW} y es usable de inmediato;
 * un admin lo pasa a {@code VERIFIED}. {@code MERGED} es terminal: el elemento se conserva
 * (otros servicios referencian su id) y apunta al canónico mediante {@code mergedInto}.
 */
public enum CatalogStatus {
    PENDING_REVIEW,
    VERIFIED,
    MERGED;

    public boolean isActive() {
        return this != MERGED;
    }
}
