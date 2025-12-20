package com.trackIt.user_service.service;

import com.trackIt.user_service.dto.*;
import com.trackIt.user_service.exception.*;
import com.trackIt.user_service.mapper.UserMapper;
import com.trackIt.user_service.model.AuthProvider;
import com.trackIt.user_service.model.Users;
import com.trackIt.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final AuthenticationManager authenticationManager;
    private final ExternalServiceClient externalServiceClient;

    /**
     * Register new user with email/password
     */
    @Transactional
    public ApiResponse<?> register(RegisterRequest request) {
        log.info("Registering new user with employeeId: {}", request.getEmployeeId());

        // Validate if user already exists
        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new UserAlreadyExistsException("Employee ID already registered");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        // Verify role exists
        externalServiceClient.verifyRoleExists(request.getRoleId());

        // Verify company exists
        externalServiceClient.verifyCompanyExists(request.getCompanyId());

        // Create user
        Users user = Users.builder()
                .employeeId(request.getEmployeeId())
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roleId(request.getRoleId())
                .companyId(request.getCompanyId())
                .provider(AuthProvider.LOCAL)
                .isAccountLocked(false)
                .isDeleted(false)
                .isEmailVerified(false)
                .build();

        userRepository.save(user);

        // Send OTP for email verification
        otpService.generateAndSendOtp(request.getEmail());

        log.info("User registered successfully: {}", request.getEmployeeId());
        return ApiResponse.success(
                "Registration successful! Please verify your email with the OTP sent to " + request.getEmail()
        );
    }

    /**
     * Login with email/password
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for employeeId: {}", request.getEmployeeId());

        Users user = userRepository.findByEmployeeId(request.getEmployeeId())
                .orElseThrow(() -> new UserNotFoundException("Invalid employee ID or password"));

        // Check if account is locked
        if (user.getIsAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support.");
        }

        // Check if account is deleted
        if (user.getIsDeleted()) {
            throw new UserNotFoundException("Invalid employee ID or password");
        }

        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmployeeId(),
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException e) {
            log.error("Authentication failed for: {}", request.getEmployeeId());
            throw new InvalidCredentialsException("Invalid employee ID or password");
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("Login successful for: {}", request.getEmployeeId());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserMapper.toResponse(user))
                .build();
    }

    /**
     * Send OTP to email
     */
    public ApiResponse<?> sendOtp(OtpRequest request) {
        log.info("Sending OTP to email: {}", request.getEmail());

        // Verify email exists
        if (!userRepository.existsByEmail(request.getEmail())) {
            throw new UserNotFoundException("Email not registered");
        }

        otpService.generateAndSendOtp(request.getEmail());

        return ApiResponse.success("OTP sent successfully to " + request.getEmail());
    }

    /**
     * Verify OTP
     */
    @Transactional
    public ApiResponse<?> verifyOtp(OtpVerifyRequest request) {
        log.info("Verifying OTP for email: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Email not registered"));

        // Verify OTP
        otpService.verifyOtp(request.getEmail(), request.getOtp());

        // Update user's email verification status
        user.setIsEmailVerified(true);
        userRepository.save(user);

        log.info("Email verified successfully for: {}", request.getEmail());
        return ApiResponse.success("Email verified successfully! You can now login.");
    }

    /**
     * Refresh access token
     */
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing access token");

        String employeeId = jwtService.extractUsername(refreshToken);
        Users user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!jwtService.validateToken(refreshToken, user)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserMapper.toResponse(user))
                .build();
    }

    /**
     * OAuth2 login/registration
     */
    @Transactional
    public AuthResponse oauth2Login(OAuth2UserInfo oauth2User, Long roleId, Long companyId) {
        log.info("OAuth2 login attempt for email: {} via {}", oauth2User.getEmail(), oauth2User.getProvider());

        Users user = userRepository.findByEmail(oauth2User.getEmail())
                .orElse(null);

        if (user == null) {
            // Register new OAuth2 user
            log.info("Creating new OAuth2 user for: {}", oauth2User.getEmail());

            // Verify role and company
            externalServiceClient.verifyRoleExists(roleId);
            externalServiceClient.verifyCompanyExists(companyId);

            // Generate unique employeeId
            String employeeId = generateEmployeeId(oauth2User.getEmail());

            user = Users.builder()
                    .employeeId(employeeId)
                    .name(oauth2User.getName())
                    .email(oauth2User.getEmail())
                    .password(null) // No password for OAuth users
                    .roleId(roleId)
                    .companyId(companyId)
                    .provider(AuthProvider.valueOf(oauth2User.getProvider().toUpperCase()))
                    .providerId(oauth2User.getProviderId())
                    .isAccountLocked(false)
                    .isDeleted(false)
                    .isEmailVerified(true) // OAuth emails are pre-verified
                    .build();

            userRepository.save(user);
        } else {
            // Check existing user
            if (user.getIsAccountLocked()) {
                throw new AccountLockedException("Your account has been locked. Please contact support.");
            }

            if (user.getIsDeleted()) {
                throw new UserNotFoundException("Account has been deactivated");
            }
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        log.info("OAuth2 login successful for: {}", oauth2User.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .user(UserMapper.toResponse(user))
                .build();
    }

    /**
     * Generate unique employee ID from email
     */
    private String generateEmployeeId(String email) {
        String base = email.split("@")[0].toUpperCase();
        String employeeId = base;
        int counter = 1;

        while (userRepository.existsByEmployeeId(employeeId)) {
            employeeId = base + counter++;
        }

        return employeeId;
    }
}