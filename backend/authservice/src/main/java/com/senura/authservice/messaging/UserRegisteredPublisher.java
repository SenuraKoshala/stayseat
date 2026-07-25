package com.senura.authservice.messaging;

import com.senura.authservice.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Publishes the {@code UserRegistered} event so the User Service can create the
 * matching profile stub. Best-effort: a broker outage is logged, never allowed
 * to fail the registration itself.
 */
@Component
public class UserRegisteredPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserRegisteredPublisher.class);
    private static final String ROUTING_KEY = "user.registered";

    private final RabbitTemplate rabbitTemplate;

    public UserRegisteredPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(UUID userId, String email, String role) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("userId", userId);
            payload.put("email", email);
            payload.put("role", role);

            Map<String, Object> envelope = new LinkedHashMap<>();
            envelope.put("eventId", UUID.randomUUID().toString());
            envelope.put("eventType", "UserRegistered");
            envelope.put("occurredAt", Instant.now().toString());
            envelope.put("payload", payload);

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, ROUTING_KEY, envelope);
            log.info("[EVENT->MQ] UserRegistered userId={} routingKey={}", userId, ROUTING_KEY);
        } catch (Exception e) {
            log.warn("Failed to publish UserRegistered for userId={}: {}", userId, e.getMessage());
        }
    }
}
