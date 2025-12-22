package com.trackIt.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class RoleBasedAuthorizationFilter extends AbstractGatewayFilterFactory<RoleBasedAuthorizationFilter.Config> {

    public RoleBasedAuthorizationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String userRole = request.getHeaders().getFirst("X-User-Role");

            if (userRole == null || userRole.isEmpty()) {
                log.warn("No role found in request headers for path: {}", request.getPath());
                return onError(exchange, "Access denied: No role found", HttpStatus.FORBIDDEN);
            }

            // Check if user has required role
            if (config.getAllowedRoles() != null && !config.getAllowedRoles().isEmpty()) {
                boolean hasAccess = config.getAllowedRoles().stream()
                        .anyMatch(role -> role.equalsIgnoreCase(userRole));

                if (!hasAccess) {
                    log.warn("User with role {} attempted to access restricted path: {}",
                            userRole, request.getPath());
                    return onError(exchange,
                            "Access denied: Insufficient permissions",
                            HttpStatus.FORBIDDEN);
                }
            }

            log.debug("Authorization successful for role: {} on path: {}",
                    userRole, request.getPath());
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String errorResponse = String.format(
                "{\"error\": \"%s\", \"status\": %d, \"timestamp\": \"%s\", \"path\": \"%s\"}",
                message,
                status.value(),
                java.time.Instant.now().toString(),
                exchange.getRequest().getPath()
        );

        byte[] bytes = errorResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        private List<String> allowedRoles;

        public Config() {}

        public Config(String... roles) {
            this.allowedRoles = Arrays.asList(roles);
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }
    }
}