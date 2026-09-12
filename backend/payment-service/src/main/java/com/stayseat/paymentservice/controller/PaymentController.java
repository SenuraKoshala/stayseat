package com.stayseat.paymentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stayseat.paymentservice.config.AuthUtil;
import com.stayseat.paymentservice.dto.ApiResponse;
import com.stayseat.paymentservice.dto.PaymentDtos.ChargeRequest;
import com.stayseat.paymentservice.dto.PaymentDtos.TransactionResponse;
import com.stayseat.paymentservice.dto.PaymentDtos.WebhookRequest;
import com.stayseat.paymentservice.exception.ApiException;
import com.stayseat.paymentservice.gateway.WebhookSignatureVerifier;
import com.stayseat.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final WebhookSignatureVerifier signatureVerifier;
    private final ObjectMapper objectMapper;

    public PaymentController(PaymentService paymentService,
                              WebhookSignatureVerifier signatureVerifier,
                              ObjectMapper objectMapper) {
        this.paymentService = paymentService;
        this.signatureVerifier = signatureVerifier;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/charge")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<TransactionResponse> charge(@Valid @RequestBody ChargeRequest request) {
        return ApiResponse.of(paymentService.charge(request, AuthUtil.currentUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionResponse> getById(@PathVariable UUID id) {
        return ApiResponse.of(paymentService.getById(id, AuthUtil.currentUser()));
    }

    @GetMapping("/booking/{bookingId}")
    public ApiResponse<List<TransactionResponse>> getByBooking(@PathVariable UUID bookingId) {
        return ApiResponse.of(paymentService.getByBooking(bookingId, AuthUtil.currentUser()));
    }

    /**
     * Public endpoint - deliberately NOT behind JWT auth (see SecurityConfig
     * and API_CONTRACT.md §4.5). The request body is read as a raw string so
     * the HMAC signature can be verified over the exact bytes the gateway
     * signed, before it's parsed into a DTO.
     */
    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.OK)
    public void webhook(@RequestBody String rawBody,
                         @RequestHeader(value = "X-Gateway-Signature", required = false) String signature) {
        if (!signatureVerifier.isValid(rawBody, signature)) {
            throw ApiException.invalidWebhookSignature();
        }

        WebhookRequest request;
        try {
            request = objectMapper.readValue(rawBody, WebhookRequest.class);
        } catch (Exception e) {
            throw ApiException.invalidWebhookPayload("Could not parse webhook payload.");
        }

        paymentService.handleWebhook(request);
    }
}
