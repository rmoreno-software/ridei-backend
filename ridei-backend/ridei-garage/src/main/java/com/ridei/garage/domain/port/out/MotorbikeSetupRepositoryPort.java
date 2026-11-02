package com.ridei.garage.domain.port.out;

import com.ridei.garage.domain.model.MotorbikeId;
import com.ridei.garage.domain.model.MotorbikeSetup;

public interface MotorbikeSetupRepositoryPort {
    /** Devuelve siempre un setup; vacío si la moto todavía no tiene nada registrado. */
    MotorbikeSetup findByMotorbikeId(MotorbikeId motorbikeId);

    void save(MotorbikeSetup setup);

    /** Borra todo el histórico de setup de la moto. No falla si no había nada. */
    void deleteByMotorbikeId(MotorbikeId motorbikeId);
}
