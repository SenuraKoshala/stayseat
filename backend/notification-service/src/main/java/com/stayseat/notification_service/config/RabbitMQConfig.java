package com.stayseat.notification_service.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    // Exchange
    public static final String EXCHANGE = "stayseat.exchange";

    // Queues
    public static final String HOTEL_QUEUE = "notification.hotel";
    public static final String RESTAURANT_QUEUE = "notification.restaurant";
    public static final String PAYMENT_QUEUE = "notification.payment";

    // Routing Keys
    public static final String HOTEL_ROUTING_KEY = "hotel.confirmed";
    public static final String RESTAURANT_ROUTING_KEY = "restaurant.confirmed";
    public static final String PAYMENT_ROUTING_KEY = "payment.#";

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue hotelQueue() {
        return new Queue(HOTEL_QUEUE, true);
    }

    @Bean
    public Queue restaurantQueue() {
        return new Queue(RESTAURANT_QUEUE, true);
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(PAYMENT_QUEUE, true);
    }

    @Bean
    public Binding hotelBinding() {
        return BindingBuilder.bind(hotelQueue())
                .to(topicExchange())
                .with(HOTEL_ROUTING_KEY);
    }

    @Bean
    public Binding restaurantBinding() {
        return BindingBuilder.bind(restaurantQueue())
                .to(topicExchange())
                .with(RESTAURANT_ROUTING_KEY);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder.bind(paymentQueue())
                .to(topicExchange())
                .with(PAYMENT_ROUTING_KEY);
    }
}