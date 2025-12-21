package com.trackIt.api_gateway.config;
import com.trackIt.api_gateway.filter.AuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway configuration class
 * Routes can be defined here programmatically or in application.yml
 * Currently using application.yml for route definitions
 */
@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;

    // Inject AuthenticationFilter
    @Autowired
    public GatewayConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ---------- PUBLIC ----------
                .route("user-auth", r -> r
                        .path("/api/auth/**")
                        .uri("lb://USER-SERVICE2"))

                .route("public-roles", r -> r
                        .path("/api/roles")
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("public-companies", r -> r
                        .path("/api/companies/active")
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------- PROTECTED ----------
                .route("user-auth", r -> r
                        .path("/api/auth/register")
                        .uri("lb://USER-SERVICE2"))

                .route("user-management", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("independent-protected", r -> r
                        .path("/api/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .build();
    }


//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                // User Service - Auth endpoints (public)
//                .route("user-auth", r -> r
//                        .path("/api/auth/**")
//                        .uri("lb://USER-SERVICE2"))
//
//                .route("roles-auth", r -> r
//                        .path("/api/roles", "/api/companies")
//                        .uri("lb://INDEPENDENT-SERVICES"))
//
//                // User Service - User management (protected)
//                .route("user-management", r -> r
//                        .path("/api/users/**")
//                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
//                        .uri("lb://USER-SERVICE"))
//
//                .build();
//    }
}