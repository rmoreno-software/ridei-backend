package com.ridei.sessions.domain.exception;

/** Un servicio del que depende el caso de uso (catálogo de circuitos, garaje) no responde. */
public class ExternalServiceUnavailableException extends RuntimeException {
    public ExternalServiceUnavailableException(String service, Throwable cause) {
        super(service + " is not available right now, please retry later", cause);
    }
}
