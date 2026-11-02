package com.ridei.garage.infrastructure.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.ridei.garage.domain.port.out.EventPublisherPort;

import lombok.AllArgsConstructor;

/**
 * Publica eventos de dominio en el bus de eventos de Spring (en proceso). Los listeners deciden
 * en qué fase de la transacción reaccionan ({@code @TransactionalEventListener}).
 */
@Component
@AllArgsConstructor
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Object event) {
        publisher.publishEvent(event);
    }
}
