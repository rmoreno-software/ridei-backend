package com.ridei.sessions.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.ridei.sessions.domain.model.CatalogVenue;
import com.ridei.sessions.domain.model.VenueId;

/**
 * Catálogo de circuitos ({@code ridei-circuits}) sin geometría, para tiempos manuales: a
 * diferencia de {@link LayoutCatalogPort}, un trazado sin meta ni dibujo es válido.
 */
public interface VenueCatalogPort {

    /**
     * @return vacío si el complejo no existe
     * @throws com.ridei.sessions.domain.exception.ExternalServiceUnavailableException si el
     *     catálogo no responde
     */
    Optional<CatalogVenue> findVenue(VenueId venueId);

    /**
     * Búsqueda por texto libre (nombre del complejo o de sus pistas), de más a menos parecido.
     *
     * @throws com.ridei.sessions.domain.exception.ExternalServiceUnavailableException si el
     *     catálogo no responde
     */
    List<CatalogVenue> search(String text, int limit);
}
