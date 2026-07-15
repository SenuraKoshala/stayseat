package com.senura.authservice.service;

import com.senura.authservice.dto.*;
import com.senura.authservice.entity.AuthUser;
import com.senura.authservice.exception.EmailAlreadyExistsException;
import com.senura.authservice.exception.InvalidCredentialsException;
import com.senura.authservice.exception.InvalidTokenException;
import com.senura.authservice.repository.AuthUserRepository;
import com.senura.authservice.security.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (authUserRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        AuthUser user = new AuthUser();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setVerified(false);
        user.setCreatedAt(Instant.now());

        AuthUser saved = authUserRepository.save(user);

        // TODO once the message broker is wired up:
        // eventPublisher.publish("UserRegistered", new UserRegisteredEvent(saved.getId(), saved.getEmail(), saved.getRole()));

        return RegisterResponse.builder()
                .userId(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        AuthUser user = authUserRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000) // seconds, per contract
                .build();
    }

    public AccessTokenResponse refresh(RefreshRequest request) {
        String token = request.getRefreshToken();

        if (!jwtUtil.isTokenValid(token)) {
            throw new InvalidTokenException("Refresh token is invalid or expired.");
        }

        Claims claims = jwtUtil.extractAllClaims(token);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new InvalidTokenException("Provided token is not a refresh token.");
        }

        UUID userId = UUID.fromString(claims.getSubject());
        AuthUser user = authUserRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User no longer exists."));

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        return AccessTokenResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtUtil.getAccessTokenExpiryMs() / 1000)
                .build();
    }

    public ValidateResponse validate(String token) {
        boolean valid = jwtUtil.isTokenValid(token);
        if (!valid) {
            return ValidateResponse.builder().valid(false).build();
        }
        Claims claims = jwtUtil.extractAllClaims(token);
        return ValidateResponse.builder()
                .valid(true)
                .userId(UUID.fromString(claims.getSubject()))
                .role(claims.get("role", String.class))
                .build();
    }
}