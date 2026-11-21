package com.ridei.sessions.domain.exception;

/** También para sesiones de otro piloto: no se revela que existen. */
public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException() {
        super("Session not found");
    }
}
