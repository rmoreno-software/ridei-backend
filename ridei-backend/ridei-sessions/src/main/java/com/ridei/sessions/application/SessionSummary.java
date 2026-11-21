package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.Session;

/** @param personalBest la mejor vuelta de la sesión es el récord del piloto con esa moto en ese trazado */
public record SessionSummary(Session session, boolean personalBest) {}
