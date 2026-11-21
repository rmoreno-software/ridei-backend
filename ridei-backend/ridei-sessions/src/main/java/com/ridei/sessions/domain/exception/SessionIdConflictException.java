package com.ridei.sessions.domain.exception;

/** Otro piloto ya usó ese id de sesión (los genera el cliente). */
public class SessionIdConflictException extends RuntimeException {
    public SessionIdConflictException() {
        super("This session id is already used");
    }
}
