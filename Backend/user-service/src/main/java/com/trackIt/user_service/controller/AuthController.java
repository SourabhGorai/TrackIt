package com.trackIt.user_service.controller;

import com.trackIt.user_service.dto.*;
import com.trackIt.user_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for employeeId: {}", request.getEmployeeId());
        ApiResponse<?> response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login with credentials
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for employeeId: {}", request.getEmployeeId());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * Send OTP to email
     */
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<?>> sendOtp(@Valid @RequestBody OtpRequest request) {
        log.info("OTP request received for email: {}", request.getEmail());
        ApiResponse<?> response = authService.sendOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP
     */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<?>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        log.info("OTP verification request received for email: {}", request.getEmail());
        ApiResponse<?> response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String refreshToken) {
        log.info("Token refresh request received");

        // Remove "Bearer " prefix if present
        String token = refreshToken.startsWith("Bearer ")
                ? refreshToken.substring(7)
                : refreshToken;

        AuthResponse response = authService.refreshToken(token);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * OAuth2 callback endpoint (simplified - actual implementation depends on OAuth provider)
     */
    @PostMapping("/oauth2/{provider}")
    public ResponseEntity<ApiResponse<AuthResponse>> oauth2Login(
            @PathVariable String provider,
            @Valid @RequestBody OAuth2UserInfo oauth2User,
            @RequestParam Long roleId,
            @RequestParam Long companyId) {

        log.info("OAuth2 login request via: {}", provider);
        oauth2User.setProvider(provider);

        AuthResponse response = authService.oauth2Login(oauth2User, roleId, companyId);
        return ResponseEntity.ok(ApiResponse.success("OAuth2 login successful", response));
    }
}