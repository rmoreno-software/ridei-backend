package com.ridei.circuits.domain.port.out;

import java.util.List;

import com.ridei.circuits.domain.event.DomainEvent;

/**
 * Publica eventos de dominio. Hoy en proceso (Spring); mañana un broker, para que otros
 * servicios reasignen los ids de los elementos fusionados.
 */
public interface DomainEventPublisherPort {

    void publish(DomainEvent event);

    default void publishAll(List<DomainEvent> events) {
        events.forEach(this::publish);
    }
}
