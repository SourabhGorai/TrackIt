package com.trackIt.user_service.security;

import com.trackIt.user_service.dto.OAuth2UserInfo;
import com.trackIt.user_service.service.AuthService;
import com.trackIt.user_service.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Optional: OAuth2 Success Handler for handling OAuth2 login success
 * This redirects to frontend with tokens after successful OAuth2 authentication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final AuthService authService;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        try {
            // Extract user info
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String provider = extractProvider(request);
            String providerId = oAuth2User.getName();

            OAuth2UserInfo oauth2UserInfo = OAuth2UserInfo.builder()
                    .email(email)
                    .name(name)
                    .provider(provider)
                    .providerId(providerId)
                    .build();

            // You'll need to get roleId and companyId from request params or session
            // For now, using default values - adjust based on your flow
            Long roleId = 1L; // Default role
            Long companyId = 1L; // Default company

            var authResponse = authService.oauth2Login(oauth2UserInfo, roleId, companyId);

            // Redirect to frontend with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", authResponse.getAccessToken())
                    .queryParam("refreshToken", authResponse.getRefreshToken())
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed", e);
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "Authentication failed")
                    .build().toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
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