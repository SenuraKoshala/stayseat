package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.HotelBookingConfirmedPayload;
import com.stayseat.notification_service.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class HotelBookingListener {

    private final NotificationProcessorService notificationProcessorService;

    @RabbitListener(queues = RabbitMQConfig.HOTEL_QUEUE)
    public void consume(EventEnvelope<HotelBookingConfirmedPayload> event) {

        notificationProcessorService.processHotelBookingConfirmation(
                event.getPayload()
        );

    }
}