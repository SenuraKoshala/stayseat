package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.RestaurantBookingConfirmedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RestaurantBookingListener {

    @RabbitListener(queues = RabbitMQConfig.RESTAURANT_QUEUE)
    public void consume(EventEnvelope<RestaurantBookingConfirmedPayload> event) {

        log.info("Restaurant Booking Confirmed");

        log.info("Booking : {}", event.getPayload().getBookingId());

    }

}