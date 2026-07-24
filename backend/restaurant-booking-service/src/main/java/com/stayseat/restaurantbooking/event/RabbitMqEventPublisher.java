package com.stayseat.restaurantbooking.event;

import com.stayseat.restaurantbooking.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Real event publisher: sends each {@link DomainEvent} to the shared
 * {@code stayseat.exchange} topic exchange so the Notification Service (and any
 * future consumer) receives it. Marked {@link Primary} so it is used at runtime
 * in preference to {@link LoggingEventPublisher}, which is kept as an offline /
 * test fallback.
 */
@Component
@Primary
public class RabbitMqEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        String routingKey = routingKeyFor(event.eventType());
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
        log.info("[EVENT->MQ] exchange={} routingKey={} type={} eventId={}",
                RabbitConfig.EXCHANGE, routingKey, event.eventType(), event.eventId());
    }

    /**
     * Maps event types to topic routing keys. {@code restaurant.confirmed} is the
     * key the Notification Service binds its restaurant queue to; created/cancelled
     * use sibling keys that currently have no consumer (silently dropped by the
     * topic exchange) but are ready for future subscribers.
     */
    private String routingKeyFor(String eventType) {
        return switch (eventType) {
            case "RestaurantBookingCreated"   -> "restaurant.created";
            case "RestaurantBookingConfirmed" -> "restaurant.confirmed";
            case "RestaurantBookingCancelled" -> "restaurant.cancelled";
            default -> "restaurant." + eventType.toLowerCase();
        };
    }
}
