package com.ridei.apirest.ridei_apirest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Ridei REST API application.
 *
 * <p>This class serves as the bootstrap for the Spring Boot application.
 * It initializes the application context, performs component scanning, and starts
 * the embedded web server (e.g., Tomcat) to serve the REST endpoints.</p>
 *
 * <p>It is annotated with {@link SpringApplication}, which is a convenience annotation
 * that combines the following:</p>
 * <ul>
 *     <li>{@code @Configuration} - marks the class as a source of bean definitions.</li>
 *     <li>{@code @EnableAutoConfiguration} - enables Spring Boot's auto-configuration mechanism.</li>
 *     <li>{@code @ComponentScan} - enables scanning of components, configurations, and services
 *     within the current package and its subpackages.</li>
 * </ul>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 *     // To start the application from the command line:
 *     $ mvn spring-boot:run
 *
 *     // Or run the main method directly in an IDE.
 * }</pre>
 *
 * <p>Once started, the API will be accessible via the configured server port
 * (by default, <code>http://localhost:8080</code>).</p>
 *
 * @author Roger Moreno González
 * @version 1.0.0
 * @since 2025-10
 */
@SpringBootApplication
public class RideiApirestApplication {

	/**
	 * Main method that launches the Ridei REST API application.
	 *
	 * @param args command-line arguments passed to the application
	 */
	public static void main(String[] args) {
		SpringApplication.run(RideiApirestApplication.class, args);
	}

}
