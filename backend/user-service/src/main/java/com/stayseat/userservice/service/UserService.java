package com.stayseat.userservice.service;

import com.stayseat.userservice.config.CurrentUser;
import com.stayseat.userservice.dto.UserDtos.PublicUserProfileResponse;
import com.stayseat.userservice.dto.UserDtos.UpdateProfileRequest;
import com.stayseat.userservice.dto.UserDtos.UserProfileResponse;
import com.stayseat.userservice.entity.UserProfile;
import com.stayseat.userservice.exception.ApiException;
import com.stayseat.userservice.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserProfileRepository repository;

    public UserService(UserProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Called when a UserRegistered event arrives. Idempotent - a duplicate
     * delivery of the same event must not fail or overwrite an edited profile.
     */
    @Transactional
    public void createStubIfAbsent(UUID userId, String role) {
        if (repository.existsById(userId)) {
            return;
        }
        UserProfile profile = UserProfile.builder()
                .userId(userId)
                .role(role)
                .createdAt(Instant.now())
                .build();
        repository.save(profile);
    }

    public UserProfileResponse getMe(CurrentUser current) {
        return toResponse(getOrThrow(current.userId()));
    }

    public PublicUserProfileResponse getPublic(UUID id) {
        UserProfile p = getOrThrow(id);
        return new PublicUserProfileResponse(p.getUserId(), p.getFirstName(), p.getLastName(),
                p.getImageUrl(), p.getRole());
    }

    @Transactional
    public UserProfileResponse updateMe(CurrentUser current, UpdateProfileRequest request) {
        UserProfile p = getOrThrow(current.userId());
        if (request.firstName() != null) p.setFirstName(request.firstName());
        if (request.lastName() != null) p.setLastName(request.lastName());
        if (request.phone() != null) p.setPhone(request.phone());
        return toResponse(repository.save(p));
    }

    @Transactional
    public UserProfileResponse setImageUrl(CurrentUser current, String imageUrl) {
        UserProfile p = getOrThrow(current.userId());
        p.setImageUrl(imageUrl);
        return toResponse(repository.save(p));
    }

    private UserProfile getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> ApiException.notFound("User profile"));
    }

    private UserProfileResponse toResponse(UserProfile p) {
        return new UserProfileResponse(p.getUserId(), p.getFirstName(), p.getLastName(), p.getPhone(),
                p.getImageUrl(), p.getRole(), p.getCreatedAt());
    }
}
