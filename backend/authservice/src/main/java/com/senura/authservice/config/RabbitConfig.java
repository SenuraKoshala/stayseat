package com.senura.authservice.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Producer-side RabbitMQ wiring. Declares the shared exchange and a JSON
 * message converter so events are published as JSON that the other services
 * (User, Notification, ...) can deserialize. Spring Boot's auto-configured
 * RabbitTemplate picks up this single MessageConverter bean.
 */
@Configuration
public class RabbitConfig {

    /** Must match the exchange the consumers bind their queues to. */
    public static final String EXCHANGE = "stayseat.exchange";

    @Bean
    public TopicExchange stayseatExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
