package com.stayseat.paymentservice.gateway;

public record ChargeResult(boolean success, String gatewayReference, String failureReason) {

    public static ChargeResult succeeded(String gatewayReference) {
        return new ChargeResult(true, gatewayReference, null);
    }

    public static ChargeResult failed(String gatewayReference, String failureReason) {
        return new ChargeResult(false, gatewayReference, failureReason);
    }
}
