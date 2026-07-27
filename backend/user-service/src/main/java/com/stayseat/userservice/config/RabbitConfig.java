package com.stayseat.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Consumer-side RabbitMQ wiring. Declares the shared exchange plus this
 * service's own queue bound to the {@code user.registered} routing key that
 * Auth publishes to.
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "stayseat.exchange";
    public static final String USER_REGISTERED_QUEUE = "user.registered";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";

    @Bean
    public TopicExchange stayseatExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
                .to(stayseatExchange())
                .with(USER_REGISTERED_ROUTING_KEY);
    }

    /**
     * Reuse Spring Boot's ObjectMapper (JSR-310 registered) so the listener can
     * deserialize the ISO-8601 occurredAt sent by Auth (Jackson 3) back into an
     * Instant. Spring Boot's listener container factory picks up this bean.
     */
    @Bean
    public MessageConverter jacksonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
