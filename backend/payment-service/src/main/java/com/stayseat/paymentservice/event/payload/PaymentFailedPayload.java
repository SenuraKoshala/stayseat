package com.stayseat.paymentservice.event.payload;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Field-for-field match with notification-service's
 * event.payload.PaymentFailedPayload - keep both in sync.
 */
public record PaymentFailedPayload(
        UUID transactionId,
        UUID bookingId,
        String bookingType,
        UUID customerId,
        BigDecimal amount,
        String status,
        String failureReason
) {}
