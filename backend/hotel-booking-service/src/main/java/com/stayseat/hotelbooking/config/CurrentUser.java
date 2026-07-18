package com.stayseat.hotelbooking.config;

import java.util.UUID;

public record CurrentUser(UUID userId, String role) {

    public boolean hasRole(String expected) {
        return role != null && role.equalsIgnoreCase(expected);
    }
}
