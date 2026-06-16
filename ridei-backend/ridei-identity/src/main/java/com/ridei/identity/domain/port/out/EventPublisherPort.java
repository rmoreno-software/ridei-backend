package com.ridei.identity.domain.port.out;

import com.ridei.identity.domain.event.UserRegisteredEvent;

public interface EventPublisherPort {
    void publish(UserRegisteredEvent event);
}
