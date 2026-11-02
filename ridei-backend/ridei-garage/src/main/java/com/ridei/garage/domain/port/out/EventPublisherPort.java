package com.ridei.garage.domain.port.out;

public interface EventPublisherPort {
    void publish(Object event);
}
