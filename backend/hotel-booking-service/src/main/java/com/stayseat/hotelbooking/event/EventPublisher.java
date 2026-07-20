package com.stayseat.hotelbooking.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}
