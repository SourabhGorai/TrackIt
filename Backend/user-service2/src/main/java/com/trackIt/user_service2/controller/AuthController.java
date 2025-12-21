package com.trackIt.user_service2.controller;

import com.trackIt.user_service2.dto.*;
import com.trackIt.user_service2.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST request to register user with email: {}", request.getEmail());

        UserResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Registration successful. Please verify your email with the OTP sent to " + request.getEmail(),
                        response
                ));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("REST request to verify email: {}", request.getEmail());

        authService.verifyEmail(request);

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully. You can now log in")
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<?>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        log.info("REST request to resend OTP for email: {}", request.getEmail());

        authService.resendOtp(request);

        return ResponseEntity.ok(
                ApiResponse.success("OTP has been resent to " + request.getEmail())
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login user with email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("REST request to refresh access token");

        AuthResponse response = authService.refreshToken(request);

        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response)
        );
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("REST request to change password for user: {}", userDetails.getUsername());

        authService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully")
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        log.info("REST request for forgot password: {}", request.getEmail());

        authService.forgotPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset OTP has been sent to " + request.getEmail())
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("REST request to reset password for: {}", request.getEmail());

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successful. You can now log in with your new password")
        );
    }
}