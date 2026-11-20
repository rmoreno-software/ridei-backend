package com.ridei.circuits.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.Assertions.withinPercentage;

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
import com.ridei.circuits.domain.model.FinishLine;
import com.ridei.circuits.domain.model.GeoPoint;
import com.ridei.circuits.domain.model.Layout;
import com.ridei.circuits.domain.model.LayoutPath;
import com.ridei.circuits.domain.model.NameNormalizer;
import com.ridei.circuits.domain.model.Track;
import com.ridei.circuits.domain.model.Venue;
import com.ridei.circuits.domain.model.VenueId;

class SeedDataIT extends PostgisIntegrationTest {

    private static final VenueId MONTMELO = VenueId.of("10000000-0000-4000-8000-000000000001");
    private static final VenueId CALAFAT = VenueId.of("10000000-0000-4000-8000-000000000009");

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
        Venue montmelo = getVenueDetail.get(MONTMELO).venue();
        Track road = montmelo.activeTracks().get(0);

        assertThat(road.defaultLayout()).get().satisfies(gp -> {
            assertThat(gp.getName().value()).isEqualTo("GP");
            assertThat(gp.getStatus()).isEqualTo(CatalogStatus.VERIFIED);
        });
        assertThat(road.activeLayouts()).extracting(layout -> layout.getName().value())
            .containsExactlyInAnyOrder("GP", "GP con chicane", "Nacional", "Escuela");
    }

    @Test
    void calafatHasThePrincipalAsDefaultLayoutAndTheShortOneWithoutDrawing() {
        Track road = getVenueDetail.get(CALAFAT).venue().activeTracks().get(0);

        assertThat(road.defaultLayout()).get().satisfies(principal -> {
            assertThat(principal.getName().value()).isEqualTo("Principal");
            assertThat(principal.getPath()).isNotNull();
        });
        assertThat(road.activeLayouts()).filteredOn(layout -> layout.getName().value().equals("Corto"))
            .singleElement().satisfies(corto -> assertThat(corto.getPath()).isNull());
    }

    // ── Dibujos de los trazados ─────────────────────────────────────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"GP", "GP con chicane", "Nacional", "Escuela"})
    void montmeloLayoutsHaveADrawingConsistentWithTheirData(String name) {
        assertDrawingIsConsistent(layoutNamed(MONTMELO, name));
    }

    @Test
    void calafatPrincipalHasADrawingConsistentWithItsData() {
        assertDrawingIsConsistent(layoutNamed(CALAFAT, "Principal"));
    }

    /** Meta del nodo raceway=finish de OSM: la comparten los trazados que pasan por la recta. */
    @ParameterizedTest
    @ValueSource(strings = {"GP", "GP con chicane", "Nacional"})
    void montmeloLayoutsThroughTheMainStraightHaveTheFinishLineAndStartOnIt(String name) {
        Layout layout = layoutNamed(MONTMELO, name);
        FinishLine finish = layout.getFinishLine();

        assertThat(finish).as("finish line of %s", name).isNotNull();
        assertThat(finish.width().meters()).isCloseTo(17, within(0.5)); // 10 m a la izquierda + 7 m hasta el muro de boxes
        assertThat(layout.getPath().points().get(0)).isEqualTo(GeoPoint.of(41.570986, 2.262025));
        // Corta el dibujo una sola vez, con los extremos en su sitio, y el dibujo ya empieza en ella.
        assertThat(layout.getPath().startingAt(finish)).isEqualTo(layout.getPath());
    }

    @Test
    void calafatPrincipalHasTheFinishLineGivenByTheTeamAndStartsOnIt() {
        Layout principal = layoutNamed(CALAFAT, "Principal");
        FinishLine finish = principal.getFinishLine();

        assertThat(finish).isNotNull();
        assertThat(finish.width().meters()).isCloseTo(16.5, within(0.5)); // 10 m a la izquierda + 6,5 m hasta el muro de boxes
        // 40°55'56.0"N 0°50'21.9"E proyectado sobre el eje de la pista
        assertThat(principal.getPath().points().get(0)).isEqualTo(GeoPoint.of(40.932217, 0.839424));
        assertThat(principal.getPath().startingAt(finish)).isEqualTo(principal.getPath());
    }

    @Test
    void layoutsWithoutAKnownFinishLineHaveNone() {
        assertThat(layoutNamed(MONTMELO, "Escuela").getFinishLine()).isNull();
        assertThat(layoutNamed(CALAFAT, "Corto").getFinishLine()).isNull();
    }

    /** El pin del complejo cae dentro del dibujo de su trazado por defecto, o a menos de 100 m. */
    @Test
    void venueLocationIsOnTheDrawingOfItsDefaultLayout() {
        List<Map<String, Object>> rows = jdbc.queryForList("""
            SELECT v.name, ST_DWithin(ST_MakePolygon(l.path)::geography, v.location, 100) AS on_drawing
            FROM layout l JOIN track t ON t.id = l.track_id JOIN venue v ON v.id = t.venue_id
            WHERE l.path IS NOT NULL AND l.is_default
            """);

        assertThat(rows).hasSizeGreaterThanOrEqualTo(2)
            .allSatisfy(row -> assertThat(row.get("on_drawing")).as("venue %s", row.get("name")).isEqualTo(true));
    }

    /** Si dos layouts activos de un track fueran equivalentes, cualquier corrección suya daría 409. */
    @ParameterizedTest
    @ValueSource(strings = {"10000000-0000-4000-8000-000000000001", "10000000-0000-4000-8000-000000000009"})
    void noTwoActiveLayoutsOfATrackAreEquivalent(String venueId) {
        for (Track track : getVenueDetail.get(VenueId.of(venueId)).venue().activeTracks()) {
            List<Layout> layouts = track.activeLayouts();
            for (int i = 0; i < layouts.size(); i++) {
                for (int j = i + 1; j < layouts.size(); j++) {
                    assertThat(layouts.get(i).isEquivalentTo(layouts.get(j)))
                        .as("%s vs %s", layouts.get(i).getName().value(), layouts.get(j).getName().value()).isFalse();
                }
            }
        }
    }

    /** Longitud del dibujo dentro del 5 % de lengthMeters y mismo sentido que direction. */
    private static void assertDrawingIsConsistent(Layout layout) {
        LayoutPath path = layout.getPath();
        assertThat(path).as("path of %s", layout.getName().value()).isNotNull();
        assertThat(path.points()).hasSizeBetween(LayoutPath.MIN_POINTS, LayoutPath.MAX_POINTS);
        assertThat(path.points().get(0)).isNotEqualTo(path.points().get(path.points().size() - 1));
        assertThat(path.length().meters()).as("drawn length of %s", layout.getName().value())
            .isCloseTo(layout.getLength().meters(), withinPercentage(5));
        assertThat(path.direction()).isEqualTo(layout.getDirection());
    }

    private Layout layoutNamed(VenueId venueId, String name) {
        return getVenueDetail.get(venueId).venue().activeTracks().get(0).activeLayouts().stream()
            .filter(layout -> layout.getName().value().equals(name))
            .findFirst().orElseThrow();
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
