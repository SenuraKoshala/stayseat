package com.stayseat.notification_service.dto;

import com.stayseat.notification_service.enums.NotificationChannel;
import com.stayseat.notification_service.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class NotificationLogResponse {

    private UUID id;

    private UUID userId;

    private NotificationChannel channel;

    private String type;

    private NotificationStatus status;

    private Instant sentAt;

}