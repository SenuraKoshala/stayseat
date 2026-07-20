package com.stayseat.hotelbooking.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PropertyDtos {

    public record PropertyRequest(
            @NotBlank String name,
            @NotBlank String city,
            String address,
            String description
    ) {}

    public record PropertyResponse(
            UUID id,
            String name,
            String city,
            String address,
            String description,
            OffsetDateTime createdAt
    ) {}
}
