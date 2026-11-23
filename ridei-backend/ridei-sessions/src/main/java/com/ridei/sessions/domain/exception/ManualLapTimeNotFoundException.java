package com.ridei.sessions.domain.exception;

/** También para tiempos de otro piloto: no se revela que existen. */
public class ManualLapTimeNotFoundException extends RuntimeException {
    public ManualLapTimeNotFoundException() {
        super("Manual lap time not found");
    }
}
