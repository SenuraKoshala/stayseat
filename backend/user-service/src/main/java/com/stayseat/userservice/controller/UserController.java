package com.stayseat.userservice.controller;

import com.stayseat.userservice.config.AuthUtil;
import com.stayseat.userservice.config.CurrentUser;
import com.stayseat.userservice.dto.ApiResponse;
import com.stayseat.userservice.dto.UserDtos.ImageResponse;
import com.stayseat.userservice.dto.UserDtos.PublicUserProfileResponse;
import com.stayseat.userservice.dto.UserDtos.UpdateProfileRequest;
import com.stayseat.userservice.dto.UserDtos.UserProfileResponse;
import com.stayseat.userservice.service.ImageService;
import com.stayseat.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final ImageService imageService;

    public UserController(UserService userService, ImageService imageService) {
        this.userService = userService;
        this.imageService = imageService;
    }

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> me() {
        return ApiResponse.of(userService.getMe(AuthUtil.currentUser()));
    }

    @GetMapping("/{id}")
    public ApiResponse<PublicUserProfileResponse> byId(@PathVariable UUID id) {
        return ApiResponse.of(userService.getPublic(id));
    }

    @PutMapping("/me")
    public ApiResponse<UserProfileResponse> updateMe(@Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.of(userService.updateMe(AuthUtil.currentUser(), request));
    }

    @PostMapping("/me/image")
    public ApiResponse<ImageResponse> uploadImage(@RequestParam("file") MultipartFile file) {
        CurrentUser current = AuthUtil.currentUser();
        String imageUrl = imageService.store(file, current.userId());
        userService.setImageUrl(current, imageUrl);
        return ApiResponse.of(new ImageResponse(imageUrl));
    }
}
