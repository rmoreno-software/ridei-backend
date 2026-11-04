package com.ridei.circuits.infrastructure.adapter.out.identity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Llama a {@code GET /api/v1/auth/validate} de ridei-identity reenviando el token:
 * 200 ⇒ {@link TokenValidationStatus#VALID}; 401/403 ⇒ {@link TokenValidationStatus#REVOKED};
 * cualquier otra respuesta o error de red ⇒ {@link TokenValidationStatus#UNAVAILABLE}.
 */
public class IdentityTokenValidationClient implements TokenValidator {

    static final String VALIDATE_PATH = "/api/v1/auth/validate";

    private static final Logger log = LoggerFactory.getLogger(IdentityTokenValidationClient.class);

    private final RestClient identity;

    public IdentityTokenValidationClient(RestClient identity) {
        this.identity = identity;
    }

    @Override
    public TokenValidationStatus validate(String accessToken) {
        try {
            HttpStatusCode status = identity.get()
                .uri(VALIDATE_PATH)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .exchange((request, response) -> response.getStatusCode());
            return toValidationStatus(status);
        } catch (RestClientException ex) {
            log.warn("ridei-identity token validation unreachable: {}", ex.getMessage());
            return TokenValidationStatus.UNAVAILABLE;
        }
    }

    private static TokenValidationStatus toValidationStatus(HttpStatusCode status) {
        if (status.is2xxSuccessful()) return TokenValidationStatus.VALID;
        if (status.value() == 401 || status.value() == 403) return TokenValidationStatus.REVOKED;
        log.warn("ridei-identity token validation answered {}", status.value());
        return TokenValidationStatus.UNAVAILABLE;
    }
}
