package com.trackIt.api_gateway.config;

import com.trackIt.api_gateway.filter.AuthenticationFilter;
import com.trackIt.api_gateway.filter.InternalServiceAuthFilter;
import com.trackIt.api_gateway.filter.RoleBasedAuthorizationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    private final AuthenticationFilter authenticationFilter;
    private final RoleBasedAuthorizationFilter roleBasedAuthorizationFilter;
    private final InternalServiceAuthFilter internalServiceAuthFilter;

    @Autowired
    public GatewayConfig(AuthenticationFilter authenticationFilter,
                         RoleBasedAuthorizationFilter roleBasedAuthorizationFilter,
                         InternalServiceAuthFilter internalServiceAuthFilter) {
        this.authenticationFilter = authenticationFilter;
        this.roleBasedAuthorizationFilter = roleBasedAuthorizationFilter;
        this.internalServiceAuthFilter = internalServiceAuthFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ========================================
                //       INTERNAL SERVICE ROUTES
                // ========================================
                // These routes are for service-to-service communication
                // They use internal token authentication instead of JWT

                .route("internal-user-details", r -> r
                        .path("/api/users/details/**", "/api/users/public/**")
                        .and().header("X-Internal-Service")
                        .filters(f -> f.filter(internalServiceAuthFilter.apply(
                                new InternalServiceAuthFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                // ---------- PUBLIC ROUTES ----------
                .route("user-auth-login", r -> r
                        .path("/api/auth/login")
                        .uri("lb://USER-SERVICE2"))

                .route("public-roles", r -> r
                        .path("/api/roles")
                        .and().method("GET")
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("public-companies", r -> r
                        .path("/api/companies/active")
                        .and().method("GET")
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("public-services-list", r -> r
                        .path("/api/services/public")
                        .and().method("GET")
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------- ADMIN ONLY ROUTES ----------
                .route("user-registration", r -> r
                        .path("/api/auth/register")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://USER-SERVICE2"))

                .route("admin-roles", r -> r
                        .path("/api/roles/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("admin-companies", r -> r
                        .path("/api/companies/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("admin-priorities", r -> r
                        .path("/api/priority/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------- ADMIN & MANAGER ROUTES ----------
                .route("manager-services-write", r -> r
                        .path("/api/services/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("manager-companies-read", r -> r
                        .path("/api/companies", "/api/companies/deleted")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------- AUTHENTICATED ROUTES (ALL ROLES) ----------
                .route("authenticated-services-read", r -> r
                        .path("/api/services/**")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("authenticated-priorities-read", r -> r
                        .path("/api/priority")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("authenticated-validate", r -> r
                        .path("/api/roles/validate/**", "/api/companies/validate/**")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("user-management", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                // ========================================
                //       SLA SERVICE GATEWAY ROUTES
                // ========================================

                // ---------- ADMIN ONLY ROUTES (SLA) ----------
                .route("admin-sla-delete", r -> r
                        .path("/api/sla/**")
                        .and().method("DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://SLA-SERVICE"))

                // ---------- ADMIN & MANAGER ROUTES (SLA) ----------
                .route("manager-sla-write", r -> r
                        .path("/api/sla", "/api/sla/rule/**")
                        .and().method("POST", "PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://SLA-SERVICE"))

                // ---------- ADMIN, MANAGER & REPORTER ROUTES (SLA) ----------
                .route("reporter-sla-read", r -> r
                        .path("/api/sla", "/api/sla/service/**", "/api/sla/priority/**")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "REPORTER"))))
                        .uri("lb://SLA-SERVICE"))

                // ---------- ALL AUTHENTICATED ROLES (SLA) ----------
                .route("authenticated-sla-read", r -> r
                        .path("/api/sla/rule/**")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "REPORTER", "SUPPORT_ENGINEER"))))
                        .uri("lb://SLA-SERVICE"))

                // ========================================
                //       INCIDENT SERVICE ROUTES
                // ========================================

                .route("incident-service-protected", r -> r
                        .path("/api/incidents/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INCIDENT-SERVICE"))


                .build();
    }
}