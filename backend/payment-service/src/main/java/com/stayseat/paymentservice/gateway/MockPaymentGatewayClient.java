package com.stayseat.paymentservice.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Placeholder implementation used until the team integrates a real gateway
 * (Stripe or PayHere - CA01 report §5.7). It simulates a synchronous charge:
 *   - simulateFailure=true always declines (lets the frontend/demo exercise
 *     the PaymentFailed path without needing a real declined card).
 *   - otherwise it always succeeds and returns a fake gateway reference.
 *
 * To swap in a real gateway: implement PaymentGatewayClient again (e.g.
 * StripePaymentGatewayClient calling the Stripe Java SDK), annotate it
 * @Component too, and put this class behind @Profile("local-no-gateway") so
 * only one PaymentGatewayClient bean exists at a time.
 */
@Component
public class MockPaymentGatewayClient implements PaymentGatewayClient {

    private static final Logger log = LoggerFactory.getLogger(MockPaymentGatewayClient.class);

    @Override
    public ChargeResult charge(UUID customerId, UUID bookingId, BigDecimal amount, String currency, boolean simulateFailure) {
        log.info("[MOCK GATEWAY] charging customerId={} bookingId={} amount={} {}",
                customerId, bookingId, amount, currency);

        // Real gateways issue a reference for the attempt whether it succeeds
        // or is declined (e.g. Stripe's PaymentIntent id), so a failed charge
        // still has something to look up later via the webhook. We mirror
        // that here rather than leaving gatewayReference null on failure.
        String gatewayReference = "mock_" + UUID.randomUUID();

        if (simulateFailure) {
            return ChargeResult.failed(gatewayReference, "Card declined by issuing bank (simulated).");
        }

        return ChargeResult.succeeded(gatewayReference);
    }
}
