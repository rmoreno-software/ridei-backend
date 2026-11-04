package com.ridei.circuits.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class ValueObjectsTest {

    @Test
    void geoPointValidatesRanges() {
        assertThat(GeoPoint.of(90, 180)).isNotNull();
        assertThatThrownBy(() -> GeoPoint.of(90.0001, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoPoint.of(0, -180.0001)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoPoint.of(Double.NaN, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void geoPointComputesGreatCircleDistance() {
        // 0.01° de latitud ≈ 1112 m
        Distance distance = GeoPoint.of(41.0, 2.0).distanceTo(GeoPoint.of(41.01, 2.0));

        assertThat(distance.meters()).isCloseTo(1112, within(2.0));
    }

    @Test
    void countryCodeIsIsoAlpha2AndUppercased() {
        assertThat(CountryCode.of(" es ").value()).isEqualTo("ES");
        assertThatThrownBy(() -> CountryCode.of("34")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CountryCode.of("ESP")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> CountryCode.of("XX")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void lengthMustBePositive() {
        assertThatThrownBy(() -> Length.ofMeters(0)).isInstanceOf(IllegalArgumentException.class);
        assertThat(Length.ofMeters(4657).isWithinToleranceOf(Length.ofMeters(4682), 25)).isTrue();
        assertThat(Length.ofMeters(4657).isWithinToleranceOf(Length.ofMeters(4683), 25)).isFalse();
    }

    @Test
    void turnsAreOptionalButNotNegative() {
        assertThat(Turns.ofNullable(null)).isNull();
        assertThat(Turns.ofNullable(0)).isEqualTo(new Turns(0));
        assertThatThrownBy(() -> Turns.ofNullable(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void externalPlaceIdTreatsBlankAsAbsent() {
        assertThat(ExternalPlaceId.ofNullable("  ")).isNull();
        assertThat(ExternalPlaceId.ofNullable(" ChIJ123 ").value()).isEqualTo("ChIJ123");
    }

    @Test
    void searchRadiusIsBounded() {
        assertThat(Distance.searchRadiusOfKilometers(2).meters()).isEqualTo(2000);
        assertThatThrownBy(() -> Distance.searchRadiusOfKilometers(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Distance.searchRadiusOfKilometers(501)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void searchCriteriaRequiresPointAndRadiusTogether() {
        assertThatThrownBy(() -> VenueSearchCriteria.of(null, null, null, GeoPoint.of(41, 2), null))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(VenueSearchCriteria.of("Circuito de Jerez", null, null, null, null).normalizedText())
            .isEqualTo("jerez");
    }
}
