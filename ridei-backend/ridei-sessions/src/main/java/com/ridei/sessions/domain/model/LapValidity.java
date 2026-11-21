package com.ridei.sessions.domain.model;

/** Por qué una vuelta cuenta o no para mejor vuelta, vuelta ideal y récords. */
public enum LapValidity {
    VALID,
    /** Recorrido mucho más corto que el trazado: atajo, o el GPS cortó la vuelta. */
    TOO_SHORT,
    /** Mucho más largo: no se detectó un paso por meta (dos vueltas en una) o se pasó por boxes. */
    TOO_LONG,
    /** El GPS dejó de dar posiciones varios segundos dentro de la vuelta. */
    GPS_GAP
}
