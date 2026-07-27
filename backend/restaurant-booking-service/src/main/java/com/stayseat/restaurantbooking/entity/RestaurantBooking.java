package com.stayseat.restaurantbooking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "restaurant_bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantBooking {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "table_id", nullable = false)
    private UUID tableId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "time_slot", nullable = false, length = 10)
    private String timeSlot;

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    // Note: the double-booking guard is the partial unique index
    // uq_active_table_slot (see V1__init.sql), enforced by Postgres at commit
    // time so it holds even under concurrent booking attempts.

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
