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
 * Spring configuration class for OpenAPI / Swagger documentation.
 *
 * <p>This class defines the OpenAPI metadata, including API title, description,
 * version, contact information, license, and available server environments.
 * It is used by SpringDoc to generate Swagger UI and API documentation automatically.</p>
 *
 * <p><b>Key Responsibilities:</b></p>
 * <ul>
 *     <li>Provide a {@link OpenAPI} bean containing API metadata.</li>
 *     <li>Set contact information for API maintainers or consumers.</li>
 *     <li>Define license information to specify usage rights.</li>
 *     <li>Declare multiple server environments (local, production, etc.).</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>
 * // Swagger UI will automatically use this OpenAPI bean
 * // Access it at: http://localhost:8080/swagger-ui.html
 * </pre>
 *
 * <p>Example of metadata configuration:</p>
 * <ul>
 *     <li>Title: "Ridei API"</li>
 *     <li>Description: "REST API for the Ridei App"</li>
 *     <li>Version: "1.0.0"</li>
 *     <li>Contact: Backend Team (email: roger.moreno.software.engineer@gmail.com)</li>
 *     <li>License: Apache 2.0</li>
 *     <li>Servers: Local (http://localhost:8080), Production (https://api.tuempresa.com)</li>
 * </ul>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@Configuration
public class OpenApiConfig {

    /**
     * Creates a custom {@link OpenAPI} bean that defines API metadata and server environments.
     *
     * <p>This bean is automatically picked up by SpringDoc to generate Swagger UI and OpenAPI documentation.
     * It allows API consumers to see the endpoints, descriptions, request/response models, and available servers.</p>
     *
     * @return a configured {@link OpenAPI} instance with metadata and server information
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
