package com.ridei.circuits.domain.model;

import java.util.Locale;
import java.util.Set;

/**
 * País según ISO 3166-1 alfa-2 (p. ej. {@code ES}). No confundir con el prefijo telefónico
 * numérico que usa ridei-identity.
 */
public record CountryCode(String value) {

    private static final Set<String> ISO_COUNTRIES = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);

    public CountryCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Country code is required");
        value = value.trim().toUpperCase(Locale.ROOT);
        if (!ISO_COUNTRIES.contains(value)) {
            throw new IllegalArgumentException("Country code must be an ISO 3166-1 alpha-2 code");
        }
    }

    public static CountryCode of(String value) {
        return new CountryCode(value);
    }
}
