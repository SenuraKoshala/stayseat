package com.stayseat.paymentservice.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;

/**
 * Mirrors notification-service's RabbitMQConfig (exchange name and routing
 * key prefix must match exactly, since notification.payment is bound to
 * "payment.#" - see notification-service RabbitMQConfig.PAYMENT_ROUTING_KEY).
 *
 * Payment Service only needs the exchange + a JSON-aware RabbitTemplate; it
 * doesn't declare/bind the queues themselves (that's the consumer's job).
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "stayseat.exchange";

    public static final String PAYMENT_PROCESSED_ROUTING_KEY = "payment.processed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    /**
     * Spring AMQP's default converter (SimpleMessageConverter) does NOT do
     * JSON - it falls back to Java serialization, which the consumer's POJOs
     * don't implement. Without this bean, PaymentProcessed/PaymentFailed
     * events would fail to deserialize on the notification-service side.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
