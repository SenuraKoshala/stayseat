package com.stayseat.notification_service.service;

import com.stayseat.notification_service.dto.NotificationLogResponse;

import java.util.List;
import java.util.UUID;

public interface NotificationLogService {

    List<NotificationLogResponse> getLogsByUserId(UUID userId);

}