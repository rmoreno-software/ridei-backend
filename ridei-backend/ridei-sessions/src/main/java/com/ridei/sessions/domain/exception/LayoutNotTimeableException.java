package com.ridei.sessions.domain.exception;

/** El trazado existe pero aún no se puede cronometrar (sin meta o sin dibujo). */
public class LayoutNotTimeableException extends RuntimeException {
    public LayoutNotTimeableException() {
        super("This layout has no finish line or drawing yet, so its laps cannot be timed");
    }
}
