package com.trackIt.user_service2.service;

import com.trackIt.user_service2.client.IndependentServiceClient;
import com.trackIt.user_service2.dto.*;
import com.trackIt.user_service2.exception.*;
import com.trackIt.user_service2.mapper.UserMapper;
import com.trackIt.user_service2.model.ProviderManagers;
import com.trackIt.user_service2.model.Users;
import com.trackIt.user_service2.repository.ProviderManagerRepository;
import com.trackIt.user_service2.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final IndependentServiceClient independentServiceClient;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final ProviderManagerRepository providerManagerRepository;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new UserAlreadyExistsException("User with this employee ID already exists");
        }

        RoleResponse role = independentServiceClient.validateRole(request.getRoleId());
        if (role == null) {
            throw new ExternalServiceException("Invalid role ID provided");
        }

        CompanyResponse company = independentServiceClient.validateCompany(request.getCompanyId());
        if (company == null) {
            throw new ExternalServiceException("Invalid company ID provided");
        }

        Users user = Users.builder()
                .employeeId(request.getEmployeeId())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .email(request.getEmail())
                .roleId(request.getRoleId())
                .companyId(request.getCompanyId())
                .isAccountLocked(false)
                .isDeleted(false)
                .isEmailVerified(false)
                .build();

        Users savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        if ("PROVIDER_MANAGER".equals(role.getRole())) {

            ProviderManagers pm = ProviderManagers.builder()
                    .user(savedUser)
                    .onCall(false)
                    .build();

            providerManagerRepository.save(pm);
        }

        otpService.createAndSendOtp(savedUser.getEmail(), savedUser.getName());

        return UserMapper.toResponseWithDetails(savedUser, role.getRole(), company.getCompanyName());
    }


    @Transactional
    public void verifyEmail(VerifyOtpRequest request) {
        log.info("Verifying email for: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getIsEmailVerified()) {
            throw new InvalidOtpException("Email is already verified");
        }

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        user.setIsEmailVerified(true);
        userRepository.save(user);

        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        log.info("Email verified successfully for: {}", request.getEmail());
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        log.info("Resending OTP for: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getIsEmailVerified()) {
            throw new InvalidOtpException("Email is already verified");
        }

        otpService.createAndSendOtp(user.getEmail(), user.getName());
        log.info("OTP resent successfully to: {}", request.getEmail());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (user.getIsDeleted()) {
            throw new UserNotFoundException("User account has been deleted");
        }

        if (user.getIsAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support");
        }

        if (!user.getIsEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // ✅ Fetch role and company information
        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        // ✅ Set role name in user object for token generation
        if (role != null) {
            user.setRoleName(role.getRole());
        }

        // ✅ Generate tokens with enriched user data
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        UserResponse userResponse = UserMapper.toResponseWithDetails(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );

        log.info("User logged in successfully: {} with role: {}",
                request.getEmail(), role != null ? role.getRole() : "UNKNOWN");

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing access token");

        String userEmail = jwtService.extractUsername(request.getRefreshToken());
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        if (!jwtService.isTokenValid(request.getRefreshToken(), userDetails)) {
            throw new InvalidTokenException("Invalid or expired refresh token");
        }

        Users user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // ✅ Fetch role information
        RoleResponse role = independentServiceClient.validateRole(user.getRoleId());
        CompanyResponse company = independentServiceClient.validateCompany(user.getCompanyId());

        // ✅ Set role name for token generation
        if (role != null) {
            user.setRoleName(role.getRole());
        }

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        UserResponse userResponse = UserMapper.toResponseWithDetails(
                user,
                role != null ? role.getRole() : null,
                company != null ? company.getCompanyName() : null
        );

        log.info("Token refreshed successfully for user: {}", userEmail);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(userResponse)
                .build();
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        log.info("Changing password for user: {}", email);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", email);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Forgot password request for: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        otpService.createAndSendOtp(user.getEmail(), user.getName());
        log.info("Password reset OTP sent to: {}", request.getEmail());
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for: {}", request.getEmail());

        Users user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        otpService.verifyOtp(request.getEmail(), request.getOtp());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password reset successfully for: {}", request.getEmail());
    }
}