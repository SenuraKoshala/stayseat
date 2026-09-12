package com.stayseat.paymentservice.event;

import com.stayseat.paymentservice.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes onto the shared "stayseat.exchange" topic exchange (same
 * exchange notification-service's queues are bound to). Publish failures are
 * logged rather than thrown, so a broker outage doesn't take down the charge
 * flow itself - the Transaction is already persisted as the source of truth;
 * losing a notification is recoverable, losing the payment record isn't.
 */
@Component
public class RabbitEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RabbitEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publish(String routingKey, DomainEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, routingKey, event);
            log.info("[EVENT] published type={} eventId={} routingKey={}",
                    event.eventType(), event.eventId(), routingKey);
        } catch (AmqpException e) {
            log.error("[EVENT] failed to publish type={} eventId={} routingKey={}: {}",
                    event.eventType(), event.eventId(), routingKey, e.getMessage(), e);
        }
    }
}
