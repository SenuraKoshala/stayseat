package com.stayseat.notification_service.controller;

import com.stayseat.notification_service.dto.ApiResponse;
import com.stayseat.notification_service.dto.NotificationLogResponse;
import com.stayseat.notification_service.service.NotificationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationLogController {

    private final NotificationLogService notificationLogService;

    @GetMapping("/logs")
    public ApiResponse<List<NotificationLogResponse>> getLogs(
            @RequestParam UUID userId
    ) {

        return ApiResponse.<List<NotificationLogResponse>>builder()
                .success(true)
                .data(notificationLogService.getLogsByUserId(userId))
                .meta(null)
                .build();
    }
}