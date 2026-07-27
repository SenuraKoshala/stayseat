package com.stayseat.notification_service.event.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class HotelBookingConfirmedPayload {

    private UUID bookingId;

    private UUID customerId;

    private UUID roomId;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private BigDecimal totalAmount;

}