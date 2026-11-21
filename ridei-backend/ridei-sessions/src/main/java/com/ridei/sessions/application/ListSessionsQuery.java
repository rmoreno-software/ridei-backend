package com.ridei.sessions.application;

import com.ridei.sessions.domain.model.LayoutId;
import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.RiderId;

/** Filtros opcionales ({@code null} = todos). {@code categoryGroup}: ASPHALT, MIXED, OFF_ROAD. */
public record ListSessionsQuery(RiderId riderId, MotorbikeId motorbikeId, LayoutId layoutId, String categoryGroup) {}
