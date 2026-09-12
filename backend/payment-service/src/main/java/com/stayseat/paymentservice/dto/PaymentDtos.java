package com.stayseat.paymentservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public final class PaymentDtos {

    private PaymentDtos() {}

    /** Amount payload for inbound requests - validated, unlike the read-only {@link Money}. */
    public record MoneyRequest(
            @NotNull @DecimalMin(value = "0.01", message = "amount must be greater than zero")
            BigDecimal amount,

            @NotBlank
            @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO code, e.g. LKR")
            String currency
    ) {}

    public record ChargeRequest(
            @NotNull UUID bookingId,

            @NotBlank
            @Pattern(regexp = "HOTEL|RESTAURANT", message = "bookingType must be HOTEL or RESTAURANT")
            String bookingType,

            @NotNull @Valid MoneyRequest amount,

            // Not part of the official contract - a convenience flag so the mock
            // gateway can be told to simulate a decline while demoing/testing the
            // PaymentFailed flow, without needing a real payment gateway wired up.
            // Defaults to false and can simply be omitted in normal requests.
            Boolean simulateFailure
    ) {}

    public record TransactionResponse(
            UUID id,
            UUID bookingId,
            String bookingType,
            UUID customerId,
            Money amount,
            String status,
            String gatewayReference,
            OffsetDateTime createdAt
    ) {}

    /**
     * Payload the payment gateway calls back with once it has a final result
     * for a transaction (async confirmation path). Looked up by
     * gatewayReference, not by our internal transaction id, since that's the
     * only identifier the gateway itself knows about.
     */
    public record WebhookRequest(
            @NotBlank String gatewayReference,

            @NotBlank
            @Pattern(regexp = "SUCCEEDED|FAILED", message = "status must be SUCCEEDED or FAILED")
            String status,

            String failureReason
    ) {}
}
