package com.stayseat.notification_service.service.impl;

import com.stayseat.notification_service.entity.NotificationLog;
import com.stayseat.notification_service.enums.NotificationChannel;
import com.stayseat.notification_service.enums.NotificationStatus;
import com.stayseat.notification_service.event.payload.HotelBookingConfirmedPayload;
import com.stayseat.notification_service.event.payload.PaymentProcessedPayload;
import com.stayseat.notification_service.event.payload.RestaurantBookingConfirmedPayload;
import com.stayseat.notification_service.repository.NotificationLogRepository;
import com.stayseat.notification_service.service.EmailService;
import com.stayseat.notification_service.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NotificationProcessorServiceImpl implements NotificationProcessorService {

    private final NotificationLogRepository repository;

    private final EmailService emailService;

    @Override
    public void processHotelBookingConfirmation(HotelBookingConfirmedPayload payload) {

        emailService.sendEmail(
                "customer@example.com",
                "Hotel Booking Confirmed",
                "Your booking " + payload.getBookingId() + " has been confirmed."
        );

        saveLog(
                payload.getCustomerId(),
                "HOTEL_BOOKING_CONFIRMATION",
                NotificationStatus.SENT
        );

    }

    @Override
    public void processRestaurantBookingConfirmation(RestaurantBookingConfirmedPayload payload) {

        emailService.sendEmail(
                "customer@example.com",
                "Restaurant Booking Confirmed",
                "Your reservation has been confirmed."
        );

        saveLog(
                payload.getCustomerId(),
                "RESTAURANT_BOOKING_CONFIRMATION",
                NotificationStatus.SENT
        );

    }

    @Override
    public void processPaymentNotification(PaymentProcessedPayload payload) {

        emailService.sendEmail(
                "customer@example.com",
                "Payment Successful",
                "Payment completed successfully."
        );

        saveLog(
                payload.getCustomerId(),
                "PAYMENT_SUCCESS",
                NotificationStatus.SENT
        );

    }

    private void saveLog(
            java.util.UUID userId,
            String type,
            NotificationStatus status
    ) {

        NotificationLog log = NotificationLog.builder()
                .userId(userId)
                .channel(NotificationChannel.EMAIL)
                .type(type)
                .status(status)
                .sentAt(Instant.now())
                .build();

        repository.save(log);

    }

}