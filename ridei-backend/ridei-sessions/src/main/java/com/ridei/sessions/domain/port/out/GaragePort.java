package com.ridei.sessions.domain.port.out;

import java.util.Optional;

import com.ridei.sessions.domain.model.MotorbikeId;
import com.ridei.sessions.domain.model.MotorbikeSnapshot;

/** Garaje del piloto autenticado ({@code ridei-garage}). */
public interface GaragePort {

    /**
     * @return vacío si la moto no existe o no es del piloto autenticado
     * @throws com.ridei.sessions.domain.exception.ExternalServiceUnavailableException si el
     *     garaje no responde
     */
    Optional<MotorbikeSnapshot> findOwnMotorbike(MotorbikeId motorbikeId);
}
