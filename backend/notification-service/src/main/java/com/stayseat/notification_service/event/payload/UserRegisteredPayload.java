package com.stayseat.notification_service.event.payload;

import lombok.Data;
import java.util.UUID;

@Data
public class UserRegisteredPayload {
    private UUID userId;
    private String email;
    private String role;
}
