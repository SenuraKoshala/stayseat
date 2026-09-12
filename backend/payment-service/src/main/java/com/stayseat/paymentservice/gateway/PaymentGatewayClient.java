package com.stayseat.paymentservice.gateway;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Abstraction over the external payment gateway (§5.7 of the CA01 report:
 * Stripe or PayHere). PaymentServiceImpl only depends on this interface, so
 * swapping the mock below for a real StripePaymentGatewayClient later is a
 * one-class change - nothing in the controller/service/entity layers needs
 * to move.
 */
public interface PaymentGatewayClient {

    ChargeResult charge(UUID customerId, UUID bookingId, BigDecimal amount, String currency, boolean simulateFailure);
}
