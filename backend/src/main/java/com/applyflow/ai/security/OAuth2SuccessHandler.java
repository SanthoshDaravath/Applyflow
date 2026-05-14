package com.applyflow.ai.security;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.repository.UserRepository;
import com.applyflow.ai.service.GmailSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final GmailSyncService gmailSyncService;
    private final String frontendUrl;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OAuth2AuthorizedClientService authorizedClientService,
            GmailSyncService gmailSyncService,
            @Value("${app.cors.allowed-origins}") String frontendUrl) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.authorizedClientService = authorizedClientService;
        this.gmailSyncService = gmailSyncService;
        this.frontendUrl = frontendUrl.split(",")[0].trim();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = String.valueOf(principal.getAttributes().getOrDefault("email", principal.getName()));
        String fullName = String.valueOf(principal.getAttributes().getOrDefault("name", email.split("@")[0]));
        String providerId = String.valueOf(principal.getAttributes().getOrDefault("sub", UUID.randomUUID().toString()));

        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .map(existing -> {
                    existing.setFullName(fullName);
                    existing.setProvider("google");
                    existing.setProviderId(providerId);
                    existing.setEmailVerified(true);
                    return existing;
                })
                .orElseGet(() -> UserEntity.builder()
                        .email(email)
                        .fullName(fullName)
                        .provider("google")
                        .providerId(providerId)
                        .role(DomainEnums.UserRole.USER)
                        .enabled(true)
                        .emailVerified(true)
                        .build());
        user = userRepository.save(user);

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient("google", authentication.getName());
        if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
            gmailSyncService.syncRecentJobEmails(user, authorizedClient.getAccessToken().getTokenValue());
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        String code = UUID.randomUUID().toString();
        ApiDtos.OAuthExchangeResponse payload = new ApiDtos.OAuthExchangeResponse(
                accessToken,
                refreshToken,
                new ApiDtos.UserResponse(user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isEmailVerified()));
        try {
            redisTemplate.opsForValue().set("oauth:code:" + code, objectMapper.writeValueAsString(payload), Duration.ofMinutes(2));
        } catch (Exception exception) {
            throw new ServletException("Failed to persist OAuth session", exception);
        }

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/auth/callback")
                .queryParam("code", code)
                .build()
                .toUriString();
        response.sendRedirect(redirectUrl);
    }
}
