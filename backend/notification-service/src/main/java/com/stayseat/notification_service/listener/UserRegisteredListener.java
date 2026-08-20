package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.UserRegisteredPayload;
import com.stayseat.notification_service.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserRegisteredListener {

    private final NotificationProcessorService notificationProcessorService;

    @RabbitListener(queues = RabbitMQConfig.USER_REGISTERED_QUEUE)
    public void consume(EventEnvelope<UserRegisteredPayload> event) {
        log.info("Received UserRegistered event for userId={}", event.getPayload().getUserId());
        notificationProcessorService.processUserRegistration(event.getPayload());
    }
}
