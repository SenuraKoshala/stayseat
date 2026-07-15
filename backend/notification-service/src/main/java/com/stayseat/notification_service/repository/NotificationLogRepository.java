package com.stayseat.notification_service.repository;

import com.stayseat.notification_service.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    List<NotificationLog> findByUserId(UUID userId);
}