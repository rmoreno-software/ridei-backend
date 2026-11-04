package com.ridei.circuits.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidator;

/**
 * Consultas y propuestas: cualquier usuario autenticado. {@code /admin/**}: rol {@code ADMIN}
 * del JWT de ridei-identity. Sin token válido o revocado → 401; con rol insuficiente → 403;
 * identity no disponible para validar el token → 503 (ver {@link JwtAuthenticationFilter}).
 */
@Configuration
public class SecurityConfig {

    static final String ADMIN_PATHS = "/api/v1/circuits/admin/**";

    @Bean
    public SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        @Value("${jwt.public-key}") String jwtPublicKey,
        TokenValidator tokenValidator,
        ObjectMapper objectMapper
    ) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtPublicKey, tokenValidator, objectMapper);
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers(ADMIN_PATHS).hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
