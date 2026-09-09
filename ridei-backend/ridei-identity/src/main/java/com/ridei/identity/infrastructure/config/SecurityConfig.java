package com.ridei.identity.infrastructure.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ridei.identity.domain.port.out.JwtPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SecurityConfig {

    private final JwtPort jwt;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(new JwtAuthenticationFilter(jwt),
                UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/api/v1/users/register",
                    "/api/v1/users/username-availability",
                    "/api/v1/users/email-availability",
                    "/api/v1/auth/google",
                    "/api/v1/auth/login",
                    "/api/v1/auth/validate",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password",
                    "/api/v1/waitlist"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("https://rideiapp.com", "https://www.rideiapp.com"));
        configuration.setAllowedMethods(List.of("POST", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/waitlist", configuration);
        return source;
    }
}
