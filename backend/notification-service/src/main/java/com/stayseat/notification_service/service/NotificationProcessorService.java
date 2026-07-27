package com.stayseat.notification_service.service;

import com.stayseat.notification_service.event.payload.HotelBookingConfirmedPayload;
import com.stayseat.notification_service.event.payload.PaymentProcessedPayload;
import com.stayseat.notification_service.event.payload.RestaurantBookingConfirmedPayload;

public interface NotificationProcessorService {

    void processHotelBookingConfirmation(HotelBookingConfirmedPayload payload);

    void processRestaurantBookingConfirmation(RestaurantBookingConfirmedPayload payload);

    void processPaymentNotification(PaymentProcessedPayload payload);

}