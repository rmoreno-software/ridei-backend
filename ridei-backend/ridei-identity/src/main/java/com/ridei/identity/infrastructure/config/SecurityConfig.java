package com.ridei.identity.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.port.out.JwtPort;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SecurityConfig {

    private final JwtPort jwt;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(new JwtAuthenticationFilter(jwt),
                UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/users/register",
                    "/api/v1/users/username-availability",
                    "/api/v1/users/email-availability",
                    "/api/v1/auth/google",
                    "/api/v1/auth/validate"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
