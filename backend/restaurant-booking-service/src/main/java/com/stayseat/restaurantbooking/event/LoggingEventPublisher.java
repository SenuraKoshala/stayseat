package com.stayseat.restaurantbooking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Placeholder implementation used until the team sets up the shared message
 * broker (RabbitMQ locally, SNS/SQS in AWS - see API_CONTRACT.md section 3.9
 * and 5). It just logs the event so you can see RestaurantBookingCreated /
 * Confirmed / Cancelled firing correctly while developing.
 *
 * To swap in a real broker later: implement EventPublisher again (e.g. a
 * RabbitMqEventPublisher that publishes to the "stayseat.exchange" topic
 * exchange with routing key "restaurant.confirmed" so the Notification Service
 * queue picks it up), annotate it @Component too, and either remove this class
 * or put it behind a @Profile("local-no-broker") so only one EventPublisher
 * bean exists at a time.
 */
@Component
public class LoggingEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("[EVENT] type={} eventId={} payload={}", event.eventType(), event.eventId(), event.payload());
    }
}
