package com.ridei.sessions.infrastructure.adapter.out.http;

/** Otro servicio rechazó el token del piloto (p. ej. revocado tras cambiar la contraseña). */
public class UpstreamUnauthorizedException extends RuntimeException {
    public UpstreamUnauthorizedException() {
        super("Your session is no longer valid, please log in again");
    }
}
