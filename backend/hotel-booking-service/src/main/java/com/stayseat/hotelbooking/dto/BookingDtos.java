package com.stayseat.hotelbooking.dto;

import com.stayseat.hotelbooking.entity.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingDtos {

    public record CreateBookingRequest(
            @NotNull UUID roomId,
            @NotNull @FutureOrPresent LocalDate checkInDate,
            @NotNull LocalDate checkOutDate
    ) {}

    public record CancelBookingRequest(String reason) {}

    public record BookingResponse(
            UUID id,
            UUID roomId,
            UUID customerId,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            BookingStatus status,
            Money totalAmount,
            OffsetDateTime createdAt
    ) {}
}
