package com.stayseat.restaurantbooking.dto;

import com.stayseat.restaurantbooking.entity.BookingStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class BookingDtos {

    public record CreateBookingRequest(
            @NotNull UUID tableId,
            @NotNull @FutureOrPresent LocalDate reservationDate,
            @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$",
                    message = "timeSlot must be in HH:mm 24-hour format, e.g. 19:30") String timeSlot,
            @NotNull @Positive Integer partySize
    ) {}

    public record CancelBookingRequest(String reason) {}

    public record BookingResponse(
            UUID id,
            UUID tableId,
            UUID customerId,
            LocalDate reservationDate,
            String timeSlot,
            Integer partySize,
            BookingStatus status,
            OffsetDateTime createdAt
    ) {}
}
