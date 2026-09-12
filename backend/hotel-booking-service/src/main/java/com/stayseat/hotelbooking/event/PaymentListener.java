package com.stayseat.hotelbooking.event;

import com.stayseat.hotelbooking.config.RabbitConfig;
import com.stayseat.hotelbooking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final BookingService bookingService;

    @RabbitListener(queues = RabbitConfig.PAYMENT_PROCESSED_QUEUE)
    public void onPaymentProcessed(EventEnvelope<PaymentProcessedPayload> event) {
        PaymentProcessedPayload payload = event.getPayload();
        if (payload != null && "HOTEL".equalsIgnoreCase(payload.getBookingType())) {
            log.info("Received PaymentProcessed event for Hotel Booking ID: {}", payload.getBookingId());
            try {
                bookingService.confirm(payload.getBookingId());
                log.info("Successfully auto-confirmed Hotel Booking ID: {}", payload.getBookingId());
            } catch (Exception e) {
                log.error("Failed to auto-confirm Hotel Booking ID {}: {}", payload.getBookingId(), e.getMessage());
            }
        }
    }
}
