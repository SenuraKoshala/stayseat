package com.stayseat.paymentservice.service.impl;

import com.stayseat.paymentservice.config.AuthUtil;
import com.stayseat.paymentservice.config.CurrentUser;
import com.stayseat.paymentservice.config.RabbitMQConfig;
import com.stayseat.paymentservice.dto.Money;
import com.stayseat.paymentservice.dto.PaymentDtos.ChargeRequest;
import com.stayseat.paymentservice.dto.PaymentDtos.TransactionResponse;
import com.stayseat.paymentservice.dto.PaymentDtos.WebhookRequest;
import com.stayseat.paymentservice.entity.BookingType;
import com.stayseat.paymentservice.entity.Transaction;
import com.stayseat.paymentservice.entity.TransactionStatus;
import com.stayseat.paymentservice.event.DomainEvent;
import com.stayseat.paymentservice.event.EventPublisher;
import com.stayseat.paymentservice.event.payload.PaymentFailedPayload;
import com.stayseat.paymentservice.event.payload.PaymentProcessedPayload;
import com.stayseat.paymentservice.exception.ApiException;
import com.stayseat.paymentservice.gateway.ChargeResult;
import com.stayseat.paymentservice.gateway.PaymentGatewayClient;
import com.stayseat.paymentservice.repository.TransactionRepository;
import com.stayseat.paymentservice.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository transactionRepository;
    private final PaymentGatewayClient gatewayClient;
    private final EventPublisher eventPublisher;

    public PaymentServiceImpl(TransactionRepository transactionRepository,
                               PaymentGatewayClient gatewayClient,
                               EventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.gatewayClient = gatewayClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TransactionResponse charge(ChargeRequest request, CurrentUser user) {
        // Idempotency guard (contract §7 "Hardening" phase calls this out
        // explicitly): don't let a retried /charge call double-charge a
        // booking that's already been paid for successfully.
        transactionRepository.findFirstByBookingIdAndStatus(request.bookingId(), TransactionStatus.SUCCEEDED)
                .ifPresent(existing -> {
                    throw ApiException.duplicateCharge(request.bookingId().toString());
                });

        BookingType bookingType = BookingType.valueOf(request.bookingType());

        Transaction transaction = Transaction.builder()
                .bookingId(request.bookingId())
                .bookingType(bookingType)
                .customerId(user.userId())
                .amount(request.amount().amount())
                .currency(request.amount().currency())
                .status(TransactionStatus.PENDING)
                .build();
        transaction = transactionRepository.save(transaction);

        boolean simulateFailure = Boolean.TRUE.equals(request.simulateFailure());
        ChargeResult result = gatewayClient.charge(
                user.userId(), request.bookingId(), request.amount().amount(), request.amount().currency(), simulateFailure);

        transaction.setGatewayReference(result.gatewayReference());
        if (result.success()) {
            transaction.setStatus(TransactionStatus.SUCCEEDED);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(result.failureReason());
        }
        transaction = transactionRepository.save(transaction);

        publishOutcome(transaction);

        return toResponse(transaction);
    }

    @Override
    public TransactionResponse getById(UUID id, CurrentUser user) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Transaction"));
        requireOwnerOrAdmin(transaction, user);
        return toResponse(transaction);
    }

    @Override
    public List<TransactionResponse> getByBooking(UUID bookingId, CurrentUser user) {
        List<Transaction> transactions = transactionRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        if (transactions.isEmpty()) {
            throw ApiException.notFound("Transaction");
        }
        requireOwnerOrAdmin(transactions.get(0), user);
        return transactions.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public void handleWebhook(WebhookRequest request) {
        Transaction transaction = transactionRepository.findByGatewayReference(request.gatewayReference())
                .orElseThrow(() -> ApiException.notFound("Transaction"));

        if (transaction.getStatus() == TransactionStatus.SUCCEEDED
                || transaction.getStatus() == TransactionStatus.REFUNDED) {
            // Already settled - webhooks can be delivered more than once
            // (at-least-once delivery is standard for gateways), so treat a
            // repeat callback as a no-op rather than an error.
            return;
        }

        TransactionStatus newStatus = TransactionStatus.valueOf(request.status());
        transaction.setStatus(newStatus);
        if (newStatus == TransactionStatus.FAILED) {
            transaction.setFailureReason(request.failureReason());
        }
        transaction = transactionRepository.save(transaction);

        publishOutcome(transaction);
    }

    private void publishOutcome(Transaction transaction) {
        if (transaction.getStatus() == TransactionStatus.SUCCEEDED) {
            PaymentProcessedPayload payload = new PaymentProcessedPayload(
                    transaction.getId(),
                    transaction.getBookingId(),
                    transaction.getBookingType().name(),
                    transaction.getCustomerId(),
                    transaction.getAmount(),
                    transaction.getStatus().name()
            );
            eventPublisher.publish(RabbitMQConfig.PAYMENT_PROCESSED_ROUTING_KEY, DomainEvent.of("PaymentProcessed", payload));
        } else if (transaction.getStatus() == TransactionStatus.FAILED) {
            PaymentFailedPayload payload = new PaymentFailedPayload(
                    transaction.getId(),
                    transaction.getBookingId(),
                    transaction.getBookingType().name(),
                    transaction.getCustomerId(),
                    transaction.getAmount(),
                    transaction.getStatus().name(),
                    transaction.getFailureReason()
            );
            eventPublisher.publish(RabbitMQConfig.PAYMENT_FAILED_ROUTING_KEY, DomainEvent.of("PaymentFailed", payload));
        }
    }

    private void requireOwnerOrAdmin(Transaction transaction, CurrentUser user) {
        if (!AuthUtil.isOwnerOrAdmin(user, transaction.getCustomerId())) {
            throw ApiException.forbidden("You do not have access to this transaction.");
        }
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getBookingId(),
                t.getBookingType().name(),
                t.getCustomerId(),
                new Money(t.getAmount(), t.getCurrency()),
                t.getStatus().name(),
                t.getGatewayReference(),
                t.getCreatedAt()
        );
    }
}
