package com.stayseat.paymentservice.service;

import com.stayseat.paymentservice.config.CurrentUser;
import com.stayseat.paymentservice.dto.PaymentDtos.ChargeRequest;
import com.stayseat.paymentservice.dto.PaymentDtos.TransactionResponse;
import com.stayseat.paymentservice.dto.PaymentDtos.WebhookRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    TransactionResponse charge(ChargeRequest request, CurrentUser user);

    TransactionResponse getById(UUID id, CurrentUser user);

    List<TransactionResponse> getByBooking(UUID bookingId, CurrentUser user);

    void handleWebhook(WebhookRequest request);
}
