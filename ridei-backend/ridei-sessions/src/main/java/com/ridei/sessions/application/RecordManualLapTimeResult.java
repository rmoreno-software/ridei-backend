package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.ManualLapTime;

/** @param created {@code false} si ya existía (reintento): se devuelve el guardado, no el enviado */
public record RecordManualLapTimeResult(ManualLapTime time, boolean created) {}
