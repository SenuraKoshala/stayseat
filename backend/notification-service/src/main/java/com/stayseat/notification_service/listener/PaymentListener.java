package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.PaymentProcessedPayload;
import com.stayseat.notification_service.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class PaymentListener {

    private final NotificationProcessorService notificationProcessorService;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void consume(EventEnvelope<PaymentProcessedPayload> event) {

        notificationProcessorService.processPaymentNotification(
                event.getPayload()
        );

    }

}