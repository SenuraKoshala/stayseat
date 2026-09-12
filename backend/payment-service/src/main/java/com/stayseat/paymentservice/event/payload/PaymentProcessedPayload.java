package com.stayseat.paymentservice.event.payload;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Field-for-field match with notification-service's
 * event.payload.PaymentProcessedPayload - keep both in sync.
 * Also consumed by Hotel Booking / Restaurant Booking services (once wired
 * up) to transition a booking from PENDING to CONFIRMED, per
 * API_CONTRACT.md §4.5 "Flow note".
 */
public record PaymentProcessedPayload(
        UUID transactionId,
        UUID bookingId,
        String bookingType,
        UUID customerId,
        BigDecimal amount,
        String status
) {}
