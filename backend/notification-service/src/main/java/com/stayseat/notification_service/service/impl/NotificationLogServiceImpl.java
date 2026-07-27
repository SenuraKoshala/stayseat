package com.stayseat.notification_service.service.impl;

import com.stayseat.notification_service.dto.NotificationLogResponse;
import com.stayseat.notification_service.entity.NotificationLog;
import com.stayseat.notification_service.repository.NotificationLogRepository;
import com.stayseat.notification_service.service.NotificationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationLogServiceImpl implements NotificationLogService {

    private final NotificationLogRepository repository;

    @Override
    public List<NotificationLogResponse> getLogsByUserId(UUID userId) {

        return repository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private NotificationLogResponse mapToResponse(NotificationLog log) {

        return NotificationLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .channel(log.getChannel())
                .type(log.getType())
                .status(log.getStatus())
                .sentAt(log.getSentAt())
                .build();
    }
}