package com.ridei.circuits.infrastructure.config;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ridei.circuits.infrastructure.adapter.in.rest.exception.ApiError;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidationStatus;
import com.ridei.circuits.infrastructure.adapter.out.identity.TokenValidator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Autentica con los access tokens ES256 emitidos por ridei-identity, en dos pasos:
 * <ol>
 *   <li>En local, con la clave pública: firma, caducidad y {@code type == "access"} (mismo
 *       enfoque que ridei-garage; código duplicado a propósito, sin módulo común).</li>
 *   <li>Online, contra ridei-identity ({@link TokenValidator}, con caché): cuenta activa y token
 *       no revocado ({@code tv}). Solo se pregunta por tokens que han pasado el paso 1.</li>
 * </ol>
 * El sujeto es el userId y el claim {@code role} se expone como {@code ROLE_<role>}. Un token
 * revocado deja la petición sin autenticar (401). Si identity no responde, se corta con 503:
 * falla cerrado, sin aceptar un token que podría estar revocado.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final PublicKey publicKey;
    private final TokenValidator tokenValidator;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(String publicKeyBase64, TokenValidator tokenValidator, ObjectMapper objectMapper) {
        this.publicKey = parsePublicKey(publicKeyBase64);
        this.tokenValidator = tokenValidator;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Claims claims = verifyLocally(token);
            if (claims != null) {
                TokenValidationStatus status = tokenValidator.validate(token);
                if (status == TokenValidationStatus.UNAVAILABLE) {
                    writeServiceUnavailable(response);
                    return;
                }
                if (status == TokenValidationStatus.VALID) authenticate(claims);
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Devuelve los claims de un access token con firma válida y sin caducar; {@code null} si no lo es. */
    private Claims verifyLocally(String token) {
        try {
            Claims claims = Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
            return "access".equals(claims.get("type", String.class)) ? claims : null;
        } catch (JwtException | IllegalArgumentException e) {
            // Token inválido o caducado: la petición sigue sin autenticar y Spring Security la rechazará.
            return null;
        }
    }

    private void authenticate(Claims claims) {
        String role = claims.get("role", String.class);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                claims.getSubject(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
            )
        );
    }

    private void writeServiceUnavailable(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
            new ApiError(503, "Service Unavailable", "Token validation is temporarily unavailable; try again shortly"));
    }

    private static PublicKey parsePublicKey(String publicKeyBase64) {
        try {
            byte[] bytes = Base64.getDecoder().decode(publicKeyBase64);
            return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(bytes));
        } catch (Exception e) {
            throw new IllegalStateException("Invalid JWT public key configuration", e);
        }
    }
}
