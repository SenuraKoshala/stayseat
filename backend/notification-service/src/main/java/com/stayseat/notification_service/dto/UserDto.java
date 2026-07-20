package com.stayseat.notification_service.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

}