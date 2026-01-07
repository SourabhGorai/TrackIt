package com.trackIt.api_gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class InternalServiceAuthFilter extends AbstractGatewayFilterFactory<InternalServiceAuthFilter.Config> {

    @Value("${app.internal-token:notification-service-secret}")
    private String expectedToken;

    public InternalServiceAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String internalService = request.getHeaders().getFirst("X-Internal-Service");
            String token = request.getHeaders().getFirst("X-Internal-Token");

            log.debug("Internal service auth check - Service: {}, Token present: {}",
                    internalService, token != null);

            // Check if this is an internal service call
            if (internalService == null || token == null) {
                log.warn("Missing internal service headers for path: {}", request.getPath());
                return onError(exchange, "Unauthorized: Missing internal service credentials",
                        HttpStatus.UNAUTHORIZED);
            }

            // Validate the internal token
            if (!expectedToken.equals(token)) {
                log.warn("Invalid internal token from service: {}", internalService);
                return onError(exchange, "Unauthorized: Invalid internal service token",
                        HttpStatus.UNAUTHORIZED);
            }

            log.debug("Internal service authenticated: {}", internalService);

            // Continue with the request
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
        // Configuration properties can be added here if needed
    }
}