package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.LapTrace;
import com.ridei.sessions.domain.model.Session;

/** Vuelta récord del piloto con la que el HUD calcula el delta en vivo. */
public record ReferenceLap(Session session, LapTrace trace) {}
