package com.ridei.circuits.infrastructure.adapter.out.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.ridei.circuits.domain.event.DomainEvent;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;

import lombok.AllArgsConstructor;

/**
 * Publica los eventos en el bus de Spring (en proceso). Los listeners eligen la fase de la
 * transacción con {@code @TransactionalEventListener}. Cuando otros servicios necesiten los
 * eventos de merge, este adaptador se sustituye por uno de broker (idealmente con outbox)
 * sin tocar dominio ni aplicación.
 */
@Component
@AllArgsConstructor
public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SpringDomainEventPublisherAdapter.class);

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(DomainEvent event) {
        log.debug("Publishing domain event {}", event);
        publisher.publishEvent(event);
    }
}
