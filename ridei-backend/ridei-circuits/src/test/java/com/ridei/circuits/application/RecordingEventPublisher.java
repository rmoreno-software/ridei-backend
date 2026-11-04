package com.ridei.circuits.application;

import java.util.ArrayList;
import java.util.List;

import com.ridei.circuits.domain.event.DomainEvent;
import com.ridei.circuits.domain.port.out.DomainEventPublisherPort;

class RecordingEventPublisher implements DomainEventPublisherPort {

    final List<DomainEvent> published = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        published.add(event);
    }
}
