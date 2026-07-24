package com.stayseat.restaurantbooking.event;

public interface EventPublisher {
    void publish(DomainEvent event);
}
