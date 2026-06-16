package com.ridei.identity.infrastructure.adapter.out.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.ridei.identity.domain.event.UserRegisteredEvent;
import com.ridei.identity.domain.port.out.EventPublisherPort;

@Component
public class LogEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(LogEventPublisherAdapter.class);

    @Override
    public void publish(UserRegisteredEvent event) {
        log.info("[EVENT] UserRegistered - userId: {}, role: {}, ocurredAt: {}",
            event.userId().value(), event.userRole(), event.ocurredAt()
        );
    }

}
