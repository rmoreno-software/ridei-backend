package com.ridei.circuits.infrastructure.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.circuits.PostgisIntegrationTest;

/**
 * Genera la especificación OpenAPI desde el contexto real y comprueba lo que el frontend
 * necesita (parámetros planos en los GET, errores documentados). Con {@code -Dopenapi.export=true}
 * la escribe en {@code docs/openapi/ridei-circuits.json}:
 *
 * <pre>./mvnw verify -pl ridei-circuits -Dit.test=OpenApiExportIT -Dtest=skip -Dsurefire.failIfNoSpecifiedTests=false -Dopenapi.export=true</pre>
 */
@AutoConfigureMockMvc
@TestPropertySource(properties = "springdoc.api-docs.enabled=true")
class OpenApiExportIT extends PostgisIntegrationTest {

    private static final Path EXPORT = Path.of("..", "docs", "openapi", "ridei-circuits.json");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void exportsADocumentedSpecification() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode spec = objectMapper.readTree(json);

        JsonNode search = spec.at("/paths/~1api~1v1~1circuits~1venues/get");
        assertThat(search.get("parameters").findValuesAsText("name")).contains("q", "country", "discipline", "lat", "lon", "radiusKm", "page", "size");
        assertThat(spec.at("/paths/~1api~1v1~1circuits~1venues/post/responses").fieldNames()).toIterable()
            .contains("200", "201", "400", "401", "409", "503");
        assertThat(spec.at("/components/schemas/ConflictApiError/properties").fieldNames()).toIterable()
            .contains("code", "candidates", "status", "message", "timestamp");

        if (Boolean.getBoolean("openapi.export")) {
            Files.createDirectories(EXPORT.getParent());
            Files.writeString(EXPORT, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec) + "\n");
        }
    }
}
