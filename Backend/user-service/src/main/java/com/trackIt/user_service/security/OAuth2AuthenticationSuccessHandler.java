package com.trackIt.user_service.security;

import com.trackIt.user_service.dto.OAuth2UserInfo;
import com.trackIt.user_service.exception.AccountLockedException;
import com.trackIt.user_service.exception.UserNotFoundException;
import com.trackIt.user_service.model.Users;
import com.trackIt.user_service.repository.UserRepository;
import com.trackIt.user_service.service.AuthService;
import com.trackIt.user_service.service.ExternalServiceClient;
import com.trackIt.user_service.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 Success Handler for Spring Security OAuth2 flow
 * Handles authentication success and redirects with JWT tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ExternalServiceClient externalServiceClient;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Extract user info from OAuth2User
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String provider = extractProvider(request);
            String providerId = oAuth2User.getName();

            log.info("OAuth2 authentication success for: {} via {}", email, provider);

            // Check if user exists
            Users user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                // New user - redirect to complete registration with role/company selection
                log.info("New OAuth2 user detected: {}", email);

                String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                        .queryParam("newUser", "true")
                        .queryParam("email", email)
                        .queryParam("name", name)
                        .queryParam("provider", provider)
                        .queryParam("providerId", providerId)
                        .build().toUriString();

                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }

            // Existing user - validate and generate tokens
            validateExistingUser(user);

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            log.info("OAuth2 login successful for existing user: {}", email);

            // Redirect to frontend with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed: {}", e.getMessage(), e);
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", e.getMessage())
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private void validateExistingUser(Users user) {
        if (user.getIsAccountLocked()) {
            throw new AccountLockedException("Your account has been locked. Please contact support.");
        }

        if (user.getIsDeleted()) {
            throw new UserNotFoundException("Account has been deactivated");
        }
    }

    private String extractProvider(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("google")) return "GOOGLE";
        if (requestUri.contains("facebook")) return "FACEBOOK";
        if (requestUri.contains("github")) return "GITHUB";
        return "UNKNOWN";
    }
}

/**
 * NOTE: This handler is used ONLY if you're using Spring Security's built-in OAuth2 configuration.
 *
 * If you're using the manual approach (sending ID tokens from frontend), this handler is NOT used.
 * In that case, use the REST endpoints directly:
 * - POST /api/auth/oauth2/google
 * - POST /api/auth/oauth2/facebook
 * - POST /api/auth/oauth2/github
 */