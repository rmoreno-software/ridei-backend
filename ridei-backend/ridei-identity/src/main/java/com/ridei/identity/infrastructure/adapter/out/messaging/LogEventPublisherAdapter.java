package com.ridei.identity.infrastructure.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.event.DomainEvent;
import com.ridei.identity.domain.event.UserGoogleAccountLinkedEvent;
import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.port.out.EventPublisherPort;

@Component
public class LogEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogEventPublisherAdapter.class);

    @Override
    public void publish(DomainEvent event) {
        switch (event) {
            case UserRegisteredEvent e -> log.info("[EVENT] UserRegistered - userId: {}, role: {}, ocurredAt: {}",
                e.userId().value(), e.userRole(), e.ocurredAt());
            case UserGoogleAccountLinkedEvent e -> log.info("[EVENT] UserGoogleAccountLinked - userId: {}, occurredAt: {}",
                e.userId().value(), e.occurredAt());
            default -> log.info("[EVENT] {}", event);
        }
    }

}
