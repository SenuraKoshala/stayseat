package com.stayseat.hotelbooking.event;

import lombok.Data;
import java.util.UUID;

@Data
public class PaymentProcessedPayload {
    private UUID transactionId;
    private UUID bookingId;
    private String bookingType;
    private UUID customerId;
    private String status;
}
