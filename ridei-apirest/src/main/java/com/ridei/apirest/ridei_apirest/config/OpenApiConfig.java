package com.ridei.apirest.ridei_apirest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class responsible for defining the OpenAPI (Swagger) documentation
 * for the REST API.
 * <p>
 *     This configuration integrates with <b>springdoc-openapi</b> to automatically generate
 *     interactive API documentation available at runtime (e.g., via Swagger UI).
 * </p>
 * Once the application starts, you can access:
 * <ul>
 *     <li><b>Swagger UI:</b> {@code http://localhost:8080/swagger-ui/index.html}</li>
 *     <li><b>OpenAPI JSON:</b> {@code http://localhost:8080/v3/api-docs}</li>
 * </ul>
 *
 * <p>
 *     Purpose:
 *     <ul>
 *         <li>Provides global metadata about the API (name, version, description).</li>
 *         <li>Specifies contact details for API maintainers or support.</li>
 *         <li>Declares license information for API usage.</li>
 *         <li>Defines the list of environments (servers) where the API can be accessed.</li>
 *     </ul>
 * </p>
 * <p>
 *     This configuration helps developers and consumers easily explore and test endpoints
 *     directly from the browser using Swagger UI.
 * </p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Defines the primary OpenAPI bean used by SpringDoc to generate
     * the Swagger documentation automatically.
     * <p>
     *     The configuration includes:
     *     <ul>
     *         <li>API title, description, and version.</li>
     *         <li>Contact details for the backend team.</li>
     *         <li>License information (Apache 2.0)</li>
     *         <li>Environment server URLs for local and production setups.</li>
     *     </ul>
     * </p>
     * <p>
     *     <b>Usage:</b>
     *     <ul>
     *         <li>The {@link Info} object defines general metadata displayed at the top of the Swagger UI.</li>
     *         <li>The {@link Contact} section provides API support contact information.</li>
     *         <li>The {@link License} section references the applicable open-source license.</li>
     *         <li>THe {@link Server} list specifies base URLs for different environments.</li>
     *     </ul>
     * </p>
     *
     * @return a fully configured {@link OpenAPI} instance containing metadata and server details
     * used by SpringDoc to render Swagger documentation.
     */
    @Bean
    public OpenAPI customOpenAPI() {

        // Create and configure the OpenAPI documentation object
        return new OpenAPI()
                .info(new Info()
                        // The main title displayed in Swagger UI
                        .title("Ridei API")

                        // General description of the API purpose and functionality
                        .description("REST API for the Ridei App")

                        // Current version of the API
                        .version("1.0.0")

                        // Contact information for developers or API consumers
                        .contact(new Contact()
                                    .name("Backend Team")
                                    .email("roger.moreno.software.engineer@gmail.com")
                                    .url("https://google.es"))

                        // License information specifying usage rights
                        .license(new License()
                                    .name("Apache 2.0")
                                    .url("http://springdoc.org")))

                // Define available servers/environments
                .servers(List.of(
                        // Local development enviroment
                        new Server().url("http://localhost:8080").description("Local enviroment"),

                        // Production environment
                        new Server().url("https://api.tuempresa.com").description("Production enviroment")
                ));
    }
}
