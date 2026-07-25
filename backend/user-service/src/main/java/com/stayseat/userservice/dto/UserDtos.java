package com.stayseat.userservice.dto;

import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public class UserDtos {

    /** Full profile - returned by GET/PUT /me to the owner. */
    public record UserProfileResponse(
            UUID userId,
            String firstName,
            String lastName,
            String phone,
            String imageUrl,
            String role,
            Instant createdAt
    ) {}

    /** Public subset - returned by GET /{id} to any authenticated caller (no phone). */
    public record PublicUserProfileResponse(
            UUID userId,
            String firstName,
            String lastName,
            String imageUrl,
            String role
    ) {}

    public record UpdateProfileRequest(
            @Size(max = 120) String firstName,
            @Size(max = 120) String lastName,
            @Size(max = 40) String phone
    ) {}

    public record ImageResponse(String imageUrl) {}
}
