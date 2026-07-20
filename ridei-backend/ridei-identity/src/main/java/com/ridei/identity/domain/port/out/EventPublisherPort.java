package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.event.DomainEvent;

public interface EventPublisherPort {
    void publish(DomainEvent event);
}
