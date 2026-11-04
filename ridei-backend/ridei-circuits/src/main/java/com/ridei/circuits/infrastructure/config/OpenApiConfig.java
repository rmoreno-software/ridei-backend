package com.ridei.circuits.infrastructure.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ridei.circuits.infrastructure.adapter.in.rest.exception.ApiError;
import com.ridei.circuits.infrastructure.adapter.in.rest.exception.ConflictApiError;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Especificación OpenAPI ({@code /v3/api-docs}, habilitada en el perfil local). Las respuestas
 * de error son comunes a todos los endpoints y se añaden aquí, en vez de repetirlas con
 * anotaciones en cada método del controlador. La exporta {@code OpenApiExportIT} a
 * {@code docs/openapi/ridei-circuits.json}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";
    private static final String API_ERROR = "ApiError";
    private static final String CONFLICT_API_ERROR = "ConflictApiError";

    private static final String CONFLICT_DESCRIPTION = """
        Conflict. `code` indicates the case:
        - `POSSIBLE_DUPLICATE_VENUE`: nearby venue(s) with a similar name; `candidates` sorted by similarity. \
        Resend with `confirmedNotDuplicate: true` to create it anyway.
        - `DUPLICATE_TRACK` / `DUPLICATE_LAYOUT`: `candidates[0]` is the existing item.
        - `CATALOG_ITEM_MERGED`: the item was merged; operate on the canonical one.
        - `CATALOG_ITEM_ALREADY_EXISTS` / `CONCURRENT_MODIFICATION`: concurrent write; reload and retry.""";

    @Bean
    public OpenAPI circuitsOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Ridei Circuits API")
                .description("Catálogo de circuitos: venues, tracks y layouts, con deduplicación y administración.")
                .version("v1"))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new Components()
                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                    .name(BEARER_SCHEME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }

    @Bean
    public OpenApiCustomizer errorResponsesCustomizer() {
        return openApi -> {
            registerErrorSchemas(openApi);
            openApi.getPaths().forEach((path, item) -> item.readOperationsMap()
                .forEach((method, operation) -> addErrorResponses(path, method, operation)));
        };
    }

    @SuppressWarnings("rawtypes")
    private static void registerErrorSchemas(OpenAPI openApi) {
        for (Class<?> type : new Class<?>[] {ApiError.class, ConflictApiError.class}) {
            Map<String, Schema> schemas = ModelConverters.getInstance().readAll(type);
            schemas.forEach(openApi.getComponents()::addSchemas);
        }
    }

    private static void addErrorResponses(String path, PathItem.HttpMethod method, Operation operation) {
        ApiResponses responses = operation.getResponses();
        responses.addApiResponse("400", error("Invalid input: `details` lists the problems", API_ERROR));
        responses.addApiResponse("401", new ApiResponse().description("Missing, invalid, expired or revoked access token (no body)"));
        if (path.contains("/admin/")) {
            responses.addApiResponse("403", new ApiResponse().description("The token does not have role ADMIN (no body)"));
        }
        if (path.contains("{")) {
            responses.addApiResponse("404", error("Venue, track or layout not found", API_ERROR));
        }
        if (method != PathItem.HttpMethod.GET) {
            responses.addApiResponse("409", error(CONFLICT_DESCRIPTION, CONFLICT_API_ERROR));
        }
        if (path.contains("/merge-into/")) {
            responses.addApiResponse("422", error("Invalid merge (same item, different disciplines, layouts of different tracks)", API_ERROR));
        }
        responses.addApiResponse("503", error("ridei-identity unreachable to validate the token; retry later", API_ERROR));
    }

    private static ApiResponse error(String description, String schema) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType("application/json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/" + schema))));
    }
}
