package com.stayseat.notification_service.client.impl;

import com.stayseat.notification_service.client.UserServiceClient;
import com.stayseat.notification_service.dto.ApiResponse;
import com.stayseat.notification_service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    private final RestClient restClient;

    private static final String USER_SERVICE =
            "http://localhost:8081/api/v1/users";

    @Override
    public UserDto getUser(UUID userId) {

        ApiResponse<UserDto> response =
                restClient.get()
                        .uri(USER_SERVICE + "/" + userId)
                        .retrieve()
                        .body(new ParameterizedTypeReference<ApiResponse<UserDto>>() {});

        return response.getData();
    }

}