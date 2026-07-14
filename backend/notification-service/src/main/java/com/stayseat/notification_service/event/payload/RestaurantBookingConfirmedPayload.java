package com.stayseat.notification_service.event.payload;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class RestaurantBookingConfirmedPayload {

    private UUID bookingId;

    private UUID customerId;

    private UUID tableId;

    private LocalDate reservationDate;

    private String timeSlot;

}