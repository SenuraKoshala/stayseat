package com.stayseat.hotelbooking.dto;

import com.stayseat.hotelbooking.entity.RoomType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class RoomDtos {

    public record RoomRequest(
            @NotNull UUID propertyId,
            @NotBlank String roomNumber,
            @NotNull RoomType type,
            @Positive Integer capacity,
            @NotNull @DecimalMin("0.0") BigDecimal pricePerNight,
            String currency
    ) {}

    public record RoomResponse(
            UUID id,
            UUID propertyId,
            String roomNumber,
            RoomType type,
            Integer capacity,
            Money pricePerNight
    ) {}
}
