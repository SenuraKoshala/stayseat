package com.stayseat.hotelbooking.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ wiring for publishing domain events.
 *
 * <p>Only the exchange is declared here - the queues and bindings are owned by
 * the Notification Service (the consumer). Declaring the same durable topic
 * exchange on both sides is idempotent, so it is safe regardless of start order.
 */
@Configuration
public class RabbitConfig {

    /** Must match the exchange the Notification Service binds its queues to. */
    public static final String EXCHANGE = "stayseat.exchange";

    @Bean
    public TopicExchange stayseatExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    /**
     * Reuse Spring Boot's auto-configured ObjectMapper (JSR-310 module registered)
     * so {@code LocalDate} / {@code OffsetDateTime} serialize as ISO-8601 strings
     * that the Notification Service (Jackson 3) can read back into its payload types.
     * Spring Boot's RabbitTemplate auto-configuration picks up this single
     * MessageConverter bean automatically.
     */
    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
