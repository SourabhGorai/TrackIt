package com.trackIt.user_service2.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class InternalServiceAuthFilter extends OncePerRequestFilter {

    @Value("${app.internal-token}")
    private String expectedToken;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String internalService = request.getHeader("X-Internal-Service");
        String token = request.getHeader("X-Internal-Token");

        // Check if this is an internal service call
        if (internalService != null) {
            log.debug("Internal service call detected from: {}", internalService);

            // Validate the token
            if (token == null || !expectedToken.equals(token)) {
                log.warn("Unauthorized internal service call from {} - Invalid or missing token", internalService);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                        String.format("{\"error\": \"Unauthorized\", \"message\": \"Invalid internal service token\"}")
                );
                return;
            }

            log.info("✅ Internal service authenticated: {}", internalService);

            // Create an authentication token for Spring Security
            // This tells Spring Security that this request is authenticated
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            internalService, // principal (service name)
                            null, // credentials (not needed)
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_INTERNAL_SERVICE"))
                    );

            // Set the authentication in the security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Security context set for internal service: {}", internalService);
        }

        // Continue with the filter chain (for both internal and regular requests)
        filterChain.doFilter(request, response);
    }
}