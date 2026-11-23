package com.ridei.sessions.domain.model;

/** De dónde sale un tiempo apuntado a mano (pantalla 22B, "¿De dónde sale este tiempo?"). */
public enum TimeSource {
    /** Crono oficial del circuito o de la tanda. */
    OFFICIAL_TIMING,
    /** Laptimer propio (AiM, Alfano, app de móvil…). */
    LAPTIMER,
    /** Cronómetro de mano. */
    STOPWATCH,
    /** Recuerdo aproximado. */
    APPROXIMATE
}
