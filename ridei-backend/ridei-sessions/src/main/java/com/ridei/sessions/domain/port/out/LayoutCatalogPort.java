package com.ridei.sessions.domain.port.out;

import java.util.Optional;

import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.LayoutSnapshot;
import com.ridei.sessions.domain.model.VenueId;

/** Catálogo de circuitos ({@code ridei-circuits}). */
public interface LayoutCatalogPort {

    /**
     * @return vacío si el venue o el trazado no existen
     * @throws com.ridei.sessions.domain.exception.LayoutNotTimeableException si existe pero no
     *     tiene meta o dibujo
     * @throws com.ridei.sessions.domain.exception.ExternalServiceUnavailableException si el
     *     catálogo no responde
     */
    Optional<LayoutSnapshot> findLayout(VenueId venueId, LayoutId layoutId);
}
