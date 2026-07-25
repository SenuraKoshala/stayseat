package com.stayseat.userservice.event;

import lombok.Data;

import java.util.UUID;

@Data
public class UserRegisteredPayload {

    private UUID userId;
    private String email;
    private String role;
}
