package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.RefreshTokenEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.exception.ApiException;
import com.applyflow.ai.repository.RefreshTokenRepository;
import com.applyflow.ai.repository.UserRepository;
import com.applyflow.ai.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ApiDtos.AuthResponse register(ApiDtos.RegisterRequest request) {
        if (!StringUtils.hasText(request.email()) || !StringUtils.hasText(request.password())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Email and password are required");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_EXISTS", "A user with this email already exists");
        }
        UserEntity user = UserEntity.builder()
                .email(request.email().toLowerCase())
                .fullName(request.fullName())
                .passwordHash(passwordEncoder.encode(request.password()))
                .provider("local")
                .role(DomainEnums.UserRole.USER)
                .enabled(true)
                .emailVerified(false)
                .build();
        user = userRepository.save(user);
        return buildTokensForUser(user);
    }

    @Transactional
    public ApiDtos.AuthResponse login(ApiDtos.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
        UserEntity user = userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_LOGIN", "Invalid credentials"));
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);
        return buildTokensForUser(user);
    }

    @Transactional
    public ApiDtos.AuthResponse refresh(ApiDtos.RefreshRequest request) {
        if (!StringUtils.hasText(request.refreshToken())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Refresh token is required");
        }
        try {
            String email = jwtService.extractUsername(request.refreshToken());
            UserEntity user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Unknown user"));
            String tokenHash = hashToken(request.refreshToken());
            RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token is not recognized"));
            if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now()) || jwtService.isExpired(request.refreshToken())) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token expired or revoked");
            }
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            return buildTokensForUser(user);
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH", "Refresh token is invalid");
        }
    }

    @Transactional
    public ApiDtos.ForgotPasswordResponse forgotPassword(ApiDtos.ForgotPasswordRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "No account matches this email"));
        String resetCode = randomCode();
        redisTemplate.opsForValue().set(resetKey(resetCode), user.getEmail(), Duration.ofMinutes(15));
        return new ApiDtos.ForgotPasswordResponse("Password reset code created for " + user.getEmail());
    }

    @Transactional
    public ApiDtos.GenericMessageResponse resetPassword(ApiDtos.ResetPasswordRequest request) {
        String email = redisTemplate.opsForValue().get(resetKey(request.token()));
        if (!StringUtils.hasText(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "RESET_TOKEN_INVALID", "Password reset token is invalid or expired");
        }
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "No account matches this reset token"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        redisTemplate.delete(resetKey(request.token()));
        refreshTokenRepository.deleteByUser(user);
        return new ApiDtos.GenericMessageResponse("Password updated successfully");
    }

    public ApiDtos.OAuthExchangeResponse exchangeOAuthCode(String code) {
        String json = redisTemplate.opsForValue().get(oauthCodeKey(code));
        if (!StringUtils.hasText(json)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OAUTH_CODE_INVALID", "OAuth code is invalid or expired");
        }
        redisTemplate.delete(oauthCodeKey(code));
        try {
            ApiDtos.OAuthExchangeResponse payload = objectMapper.readValue(json, ApiDtos.OAuthExchangeResponse.class);
            UserEntity user = userRepository.findByEmailIgnoreCase(payload.user().email())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "OAuth user not found"));
            return new ApiDtos.OAuthExchangeResponse(
                    payload.accessToken(),
                    payload.refreshToken(),
                    new ApiDtos.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isEmailVerified()));
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "OAUTH_EXCHANGE_FAILED", "Unable to exchange OAuth code");
        }
    }

    public ApiDtos.UserResponse me() {
        UserEntity user = currentUserService.currentUser();
        return new ApiDtos.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isEmailVerified());
    }

    private ApiDtos.AuthResponse buildTokensForUser(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        persistRefreshToken(user, refreshToken);
        return new ApiDtos.AuthResponse(
                accessToken,
                refreshToken,
                new ApiDtos.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isEmailVerified()),
                "Bearer",
                Instant.now().plusSeconds(jwtService.getAccessTokenTtlMinutes() * 60));
    }

    private void persistRefreshToken(UserEntity user, String refreshToken) {
        refreshTokenRepository.save(RefreshTokenEntity.builder()
                .user(user)
                .tokenHash(hashToken(refreshToken))
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTokenTtlDays() * 24L * 3600L))
                .revoked(false)
                .build());
    }

    private String randomCode() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String resetKey(String token) {
        return "reset:pwd:" + token;
    }

    private String oauthCodeKey(String code) {
        return "oauth:code:" + code;
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash refresh token", exception);
        }
    }
}
