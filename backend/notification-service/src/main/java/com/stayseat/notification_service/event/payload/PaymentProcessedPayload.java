package com.stayseat.notification_service.event.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentProcessedPayload {

    private UUID transactionId;

    private UUID bookingId;

    private String bookingType;

    private UUID customerId;

    private BigDecimal amount;

    private String status;

}