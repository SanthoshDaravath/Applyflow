package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiDtos.AuthResponse> register(@Valid @RequestBody ApiDtos.RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiDtos.AuthResponse> login(@Valid @RequestBody ApiDtos.LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiDtos.AuthResponse> refresh(@Valid @RequestBody ApiDtos.RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiDtos.ForgotPasswordResponse> forgotPassword(@Valid @RequestBody ApiDtos.ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiDtos.GenericMessageResponse> resetPassword(@Valid @RequestBody ApiDtos.ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/oauth/exchange")
    public ResponseEntity<ApiDtos.OAuthExchangeResponse> exchange(@Valid @RequestBody ApiDtos.OAuthCodeExchangeRequest request) {
        return ResponseEntity.ok(authService.exchangeOAuthCode(request.code()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiDtos.UserResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
