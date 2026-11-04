package com.ridei.circuits.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ridei.circuits.PostgisIntegrationTest;
import com.ridei.circuits.application.port.in.GetVenueDetailUseCase;
import com.ridei.circuits.domain.model.CatalogStatus;
import com.ridei.circuits.domain.model.Discipline;
import com.ridei.circuits.domain.model.NameNormalizer;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

class SeedDataIT extends PostgisIntegrationTest {

    @Autowired JdbcTemplate jdbc;
    @Autowired GetVenueDetailUseCase getVenueDetail;

    /** Si falla, la normalización de Java y la columna del seed divergen: la deduplicación fallaría en silencio. */
    @ParameterizedTest
    @ValueSource(strings = {"venue", "track", "layout"})
    void normalizedNamesMatchTheDomainNormalization(String table) {
        List<Map<String, Object>> rows = jdbc.queryForList("SELECT name, normalized_name FROM " + table);

        assertThat(rows).isNotEmpty().allSatisfy(row ->
            assertThat(row.get("normalized_name")).as("normalized_name of '%s'", row.get("name"))
                .isEqualTo(NameNormalizer.normalize((String) row.get("name"))));
    }

    @Test
    void seedsTheMainSpanishVenuesAsVerified() {
        Long verifiedSeedVenues = jdbc.queryForObject(
            "SELECT COUNT(*) FROM venue WHERE created_by IS NULL AND status = 'VERIFIED' AND country_code = 'ES'", Long.class);

        assertThat(verifiedSeedVenues).isEqualTo(11);
    }

    @Test
    void alcarrasIsOneVenueWithSeveralTracksOfDifferentDisciplines() {
        Venue alcarras = getVenueDetail.get(VenueId.of("10000000-0000-4000-8000-000000000010")).venue();

        assertThat(alcarras.activeTracks()).extracting(Track::getDiscipline)
            .contains(Discipline.ROAD_RACING, Discipline.KARTING, Discipline.FLAT_TRACK);
    }

    @Test
    void montmeloHasTheGpAsVerifiedDefaultLayout() {
        Venue montmelo = getVenueDetail.get(VenueId.of("10000000-0000-4000-8000-000000000001")).venue();
        Track road = montmelo.activeTracks().get(0);

        assertThat(road.defaultLayout()).get().satisfies(gp -> {
            assertThat(gp.getName().value()).isEqualTo("GP");
            assertThat(gp.getStatus()).isEqualTo(CatalogStatus.VERIFIED);
        });
        assertThat(road.activeLayouts()).hasSize(1);
    }

    @Test
    void generatedLocationMatchesLatitudeAndLongitude() {
        Map<String, Object> row = jdbc.queryForMap("""
            SELECT latitude, longitude, ST_Y(location::geometry) AS y, ST_X(location::geometry) AS x
            FROM venue WHERE id = '10000000-0000-4000-8000-000000000002'
            """);

        assertThat(row.get("y")).isEqualTo(row.get("latitude"));
        assertThat(row.get("x")).isEqualTo(row.get("longitude"));
    }
}
