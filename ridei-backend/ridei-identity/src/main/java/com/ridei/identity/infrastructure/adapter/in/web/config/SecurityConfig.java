package com.ridei.identity.infrastructure.adapter.in.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Infrastructure configuration class responsible for defining the application's security posture.
 * <p>
 * This class leverages Spring Security to establish a {@link SecurityFilterChain} that intercepts
 * all incoming HTTP requests. It acts as the primary firewall for the Web Adapter,
 * handling Authentication (Who are you?) and Authorization (What can you do?) rules
 * before requests reach the Controller layer.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for HTTP requests.
     * <p>
     * <b>Security Policy Definitions:</b>
     * <ul>
     * <li><b>CSRF (Cross-Site Request Forgery):</b> Disabled. This is standard practice for
     * stateless REST APIs that use token-based authentication (e.g., JWT) instead of
     * browser-session cookies.</li>
     * <li><b>Public Endpoints:</b> Grants unrestricted access ({@code permitAll}) to the
     * {@code /auth/**} path to allow user registration and login.</li>
     * <li><b>Protected Endpoints:</b> Enforces a "Deny-by-default" strategy, requiring
     * authentication for any request not explicitly whitelisted.</li>
     * </ul>
     * </p>
     *
     * @param http The {@link HttpSecurity} builder provided by Spring.
     * @return The built {@link SecurityFilterChain} bean.
     * @throws Exception if an error occurs during the configuration build process.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Desactivamos CSRF porque es una API REST (Stateless) y no usa cookies de sesión navegador
        return http
            // 1. Disable CSRF (Stateless API architecture)
            .csrf(AbstractHttpConfigurer::disable)

            // 2. Define Authorization Rules (White-listing strategy)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()    // Public access for Auth Controller
                .anyRequest().authenticated()                           // All other endpoints require a valid token/session
            )

            // 3. Build the chain
            .build();
    }
    
}
