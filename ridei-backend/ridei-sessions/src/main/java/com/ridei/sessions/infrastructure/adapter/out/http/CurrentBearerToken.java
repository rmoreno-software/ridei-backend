package com.ridei.sessions.infrastructure.adapter.out.http;

import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * El {@code Authorization} de la petición en curso, para llamar a otros servicios en nombre del
 * piloto: así circuits y garage aplican sus propias reglas (revocación, propiedad de la moto) y
 * no hace falta ninguna credencial de servicio compartida.
 */
public final class CurrentBearerToken {

    private CurrentBearerToken() {}

    public static String authorizationHeader() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            String header = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (header != null && header.startsWith("Bearer ")) return header;
        }
        throw new IllegalStateException("No bearer token in the current request");
    }
}
