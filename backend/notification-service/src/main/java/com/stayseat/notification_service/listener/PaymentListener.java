package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.PaymentProcessedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentListener {

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void consume(EventEnvelope<PaymentProcessedPayload> event) {

        log.info("Payment Event Received");

        log.info("Booking : {}", event.getPayload().getBookingId());

        log.info("Status : {}", event.getPayload().getStatus());

    }

}