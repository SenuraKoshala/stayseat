package com.stayseat.paymentservice.repository;

import com.stayseat.paymentservice.entity.Transaction;
import com.stayseat.paymentservice.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByBookingIdOrderByCreatedAtDesc(UUID bookingId);

    Optional<Transaction> findFirstByBookingIdAndStatus(UUID bookingId, TransactionStatus status);

    Optional<Transaction> findByGatewayReference(String gatewayReference);
}
