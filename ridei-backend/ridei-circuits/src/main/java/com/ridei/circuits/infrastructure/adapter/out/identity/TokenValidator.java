package com.ridei.circuits.infrastructure.adapter.out.identity;

/**
 * Comprueba online si un access token sigue siendo válido según ridei-identity. Es un asunto
 * de seguridad de la infraestructura, no del dominio del catálogo, por eso no es un puerto
 * del dominio.
 */
public interface TokenValidator {

    TokenValidationStatus validate(String accessToken);
}
