package com.ridei.circuits.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.circuits.PostgisIntegrationTest;

/**
 * Contrato que usa el mapa de la app: los trazados del seed llegan con su {@code path} por
 * HTTP, contra la base de datos real. Las consultas son las de la app (radio por defecto, 50 km).
 */
@AutoConfigureMockMvc
class LayoutPathApiIT extends PostgisIntegrationTest {

    private static final String VENUES = "/api/v1/circuits/venues";
    private static final String MONTMELO_ID = "10000000-0000-4000-8000-000000000001";
    private static final String CALAFAT_ID = "10000000-0000-4000-8000-000000000009";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void nearbyReturnsTheDrawingOfEveryMontmeloLayout() throws Exception {
        JsonNode montmelo = venueIn(call(get(VENUES + "/nearby").param("lat", "41.57").param("lon", "2.26111")), MONTMELO_ID);

        for (String name : new String[] {"GP", "GP con chicane", "Nacional", "Escuela"}) {
            assertValidPath(layout(montmelo, name).get("path"), name);
        }
    }

    @Test
    void nearbyReturnsTheDrawingOfCalafatAndNullForTheLayoutWithoutOne() throws Exception {
        JsonNode calafat = venueIn(call(get(VENUES + "/nearby").param("lat", "40.93389").param("lon", "0.84167")), CALAFAT_ID);

        assertValidPath(layout(calafat, "Principal").get("path"), "Principal");
        assertThat(layout(calafat, "Corto").get("path").isNull()).isTrue();
    }

    @Test
    void searchAndDetailAlsoReturnThePath() throws Exception {
        JsonNode search = venueIn(call(get(VENUES).param("q", "calafat")), CALAFAT_ID);
        JsonNode detail = call(get(VENUES + "/" + MONTMELO_ID)).get("venue");

        assertValidPath(layout(search, "Principal").get("path"), "Principal (search)");
        assertValidPath(layout(detail, "GP").get("path"), "GP (detail)");
    }

    private JsonNode call(MockHttpServletRequestBuilder request) throws Exception {
        String body = mvc.perform(request.with(user(TestTokens.RIDER_ID.toString()).roles("RIDER")))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    private static JsonNode venueIn(JsonNode page, String venueId) {
        return StreamSupport.stream(page.get("content").spliterator(), false)
            .map(item -> item.get("venue"))
            .filter(venue -> venue.get("id").asText().equals(venueId))
            .findFirst().orElseThrow(() -> new AssertionError("venue " + venueId + " not in " + page));
    }

    private static JsonNode layout(JsonNode venue, String name) {
        return StreamSupport.stream(venue.get("tracks").get(0).get("layouts").spliterator(), false)
            .filter(layout -> layout.get("name").asText().equals(name))
            .findFirst().orElseThrow(() -> new AssertionError("layout " + name + " not found"));
    }

    private static void assertValidPath(JsonNode path, String layout) {
        assertThat(path.isArray()).as("path of %s", layout).isTrue();
        assertThat(path.size()).as("points of %s", layout).isBetween(3, 200);
        assertThat(path).allSatisfy(point -> {
            assertThat(point.get("latitude").isNumber()).isTrue();
            assertThat(point.get("longitude").isNumber()).isTrue();
            assertThat(point.get("latitude").asDouble()).isBetween(-90.0, 90.0);
            assertThat(point.get("longitude").asDouble()).isBetween(-180.0, 180.0);
        });
        assertThat(path.get(0)).as("first point not repeated at the end of %s", layout).isNotEqualTo(path.get(path.size() - 1));
    }
}
