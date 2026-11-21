package com.ridei.sessions.infrastructure.adapter.in.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ridei.sessions.application.ReferenceLap;
import com.ridei.sessions.domain.model.Gate;
import com.ridei.sessions.domain.model.TrackGeometry;

/**
 * Lo que el HUD necesita para el delta en vivo frente al récord: la meta y las puertas de sector
 * con las que se cronometró, y la traza de la vuelta récord. Las puertas ya vienen orientadas en
 * el sentido en que se rodó.
 *
 * @param finishLine  [izquierda, derecha] mirando en el sentido de la marcha
 * @param sectorGates puerta al final de cada sector salvo el último (que acaba en meta)
 */
public record ReferenceLapResponseDTO(
    UUID sessionId,
    long timeMillis,
    List<Long> sectorsMillis,
    List<PointDTO> finishLine,
    List<List<PointDTO>> sectorGates,
    LapTraceResponseDTO trace
) {
    public static ReferenceLapResponseDTO from(ReferenceLap reference) {
        TrackGeometry geometry = reference.session().timing().reversedDirection()
            ? reference.session().layout().geometry().reversed()
            : reference.session().layout().geometry();
        List<List<PointDTO>> gates = new ArrayList<>();
        for (int k = 1; k < geometry.sectorCount(); k++) gates.add(gate(geometry.sectorGate(k)));
        var lap = reference.session().timing().lap(reference.trace().lapNumber()).orElseThrow();
        return new ReferenceLapResponseDTO(
            reference.session().id().value(),
            lap.durationMillis(),
            lap.sectorMillis(),
            gate(geometry.finishLine()),
            gates,
            LapTraceResponseDTO.fromDomain(reference.trace()));
    }

    private static List<PointDTO> gate(Gate gate) {
        return List.of(PointDTO.fromDomain(gate.left()), PointDTO.fromDomain(gate.right()));
    }
}
