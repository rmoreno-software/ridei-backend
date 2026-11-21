package com.ridei.sessions.domain.model;

/** Dónde se va el tiempo en una curva, comparando la mejor vuelta con el mejor paso por ella. */
public enum CornerLossCause {
    /** La mejor vuelta ya hizo su mejor paso por esta curva. */
    NONE,
    /** Frenó bastante antes que en el mejor paso. */
    BRAKING,
    /** Llegó más lento al vértice. */
    MIN_SPEED,
    /** Salió más lento. */
    EXIT,
    /** Velocidades parecidas pero más tiempo: la línea (más metros recorridos). */
    LINE
}
