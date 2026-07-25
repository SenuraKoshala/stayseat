package com.stayseat.userservice.event;

import com.stayseat.userservice.config.RabbitConfig;
import com.stayseat.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumes {@code UserRegistered} (published by Auth) and creates the profile
 * stub row. This is the only way profiles are created - there is no create
 * endpoint (contract 4.2).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegisteredListener {

    private final UserService userService;

    @RabbitListener(queues = RabbitConfig.USER_REGISTERED_QUEUE)
    public void onUserRegistered(EventEnvelope<UserRegisteredPayload> event) {
        UserRegisteredPayload payload = event.getPayload();
        if (payload == null || payload.getUserId() == null) {
            log.warn("Ignoring UserRegistered event with empty payload: {}", event.getEventId());
            return;
        }
        userService.createStubIfAbsent(payload.getUserId(), payload.getRole());
        log.info("Created profile stub for userId={} role={}", payload.getUserId(), payload.getRole());
    }
}
