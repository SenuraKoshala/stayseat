package com.stayseat.notification_service.client;

import com.stayseat.notification_service.dto.UserDto;

import java.util.UUID;

public interface UserServiceClient {

    UserDto getUser(UUID userId);

}