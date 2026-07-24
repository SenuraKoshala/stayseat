package com.stayseat.restaurantbooking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class TableDtos {

    public record TableRequest(
            @NotNull UUID propertyId,
            @NotBlank String tableNumber,
            @NotNull @Positive Integer capacity
    ) {}

    public record TableResponse(
            UUID id,
            UUID propertyId,
            String tableNumber,
            Integer capacity
    ) {}
}
