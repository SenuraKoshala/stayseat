package com.stayseat.notification_service.listener;

import com.stayseat.notification_service.config.RabbitMQConfig;
import com.stayseat.notification_service.event.EventEnvelope;
import com.stayseat.notification_service.event.payload.HotelBookingConfirmedPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HotelBookingListener {

    @RabbitListener(queues = RabbitMQConfig.HOTEL_QUEUE)
    public void consume(EventEnvelope<HotelBookingConfirmedPayload> event) {

        log.info("Hotel Booking Confirmed Event Received");

        log.info("Booking ID : {}", event.getPayload().getBookingId());

        log.info("Customer ID : {}", event.getPayload().getCustomerId());

    }

}