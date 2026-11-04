package com.ridei.circuits.domain.model;

/**
 * Filtros de búsqueda de venues; todos opcionales. El texto se guarda ya normalizado con
 * las mismas reglas que {@code normalized_name}. {@code near} y {@code radius} van juntos.
 */
public record VenueSearchCriteria(
    String normalizedText,
    CountryCode countryCode,
    Discipline discipline,
    GeoPoint near,
    Distance radius
) {

    public VenueSearchCriteria {
        if ((near == null) != (radius == null)) {
            throw new IllegalArgumentException("Latitude, longitude and radius must be given together");
        }
        if (normalizedText != null && normalizedText.isBlank()) normalizedText = null;
    }

    public static VenueSearchCriteria of(
        String text,
        CountryCode countryCode,
        Discipline discipline,
        GeoPoint near,
        Distance radius
    ) {
        String normalized = text == null || text.isBlank() ? null : NameNormalizer.normalize(text);
        return new VenueSearchCriteria(normalized, countryCode, discipline, near, radius);
    }

    public boolean hasText() {
        return normalizedText != null;
    }

    public boolean hasProximity() {
        return near != null;
    }
}
