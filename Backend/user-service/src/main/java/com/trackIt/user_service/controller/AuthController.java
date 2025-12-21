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
     * OAuth2 Google login with ID token
     */
    @PostMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(
            @Valid @RequestBody OAuth2LoginRequest request) {

        log.info("Google OAuth2 login request received");
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }

    /**
     * Complete OAuth2 registration for new users
     * Used when a new user logs in via OAuth2 and needs to select role/company
     */
    @PostMapping("/oauth2/complete-registration")
    public ResponseEntity<ApiResponse<AuthResponse>> completeOAuth2Registration(
            @Valid @RequestBody OAuth2CompleteRegistrationRequest request) {

        log.info("OAuth2 registration completion request for: {}", request.getEmail());
        AuthResponse response = authService.completeOAuth2Registration(request);
        return ResponseEntity.ok(ApiResponse.success("Registration completed successfully", response));
    }

    /**
     * OAuth2 Facebook login
     */
    @PostMapping("/oauth2/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> facebookLogin(
            @Valid @RequestBody OAuth2LoginRequest request) {

        log.info("Facebook OAuth2 login request received");
        AuthResponse response = authService.facebookLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Facebook login successful", response));
    }

    /**
     * OAuth2 GitHub login
     */
    @PostMapping("/oauth2/github")
    public ResponseEntity<ApiResponse<AuthResponse>> githubLogin(
            @Valid @RequestBody OAuth2LoginRequest request) {

        log.info("GitHub OAuth2 login request received");
        AuthResponse response = authService.githubLogin(request);
        return ResponseEntity.ok(ApiResponse.success("GitHub login successful", response));
    }
}