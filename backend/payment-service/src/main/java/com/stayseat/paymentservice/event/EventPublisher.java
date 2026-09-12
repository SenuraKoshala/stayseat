package com.stayseat.paymentservice.event;

public interface EventPublisher {
    void publish(String routingKey, DomainEvent event);
}
