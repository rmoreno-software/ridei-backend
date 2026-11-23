package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;

/** Filtros opcionales ({@code null} = todos). */
public record ListManualLapTimesQuery(RiderId riderId, LayoutId layoutId, MotorbikeId motorbikeId) {}
