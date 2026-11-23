package com.ridei.sessions.domain.exception;

/** Otro piloto ya usó ese id de tiempo manual (los genera el cliente). */
public class ManualLapTimeIdConflictException extends RuntimeException {
    public ManualLapTimeIdConflictException() {
        super("This manual lap time id is already used");
    }
}
