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

                // ================================================================================
                //                          INTERNAL SERVICE ROUTES
                // ================================================================================
                // Service-to-service communication using internal token authentication
                // ================================================================================

                .route("internal-user-details", r -> r
                        .path("/api/users/details/**", "/api/users/public/**")
                        .and().header("X-Internal-Service")
                        .filters(f -> f.filter(internalServiceAuthFilter.apply(
                                new InternalServiceAuthFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("internal-independent-service-validation", r -> r
                        .path("/api/services/**", "/api/priority/**", "/api/roles/validate/**", "/api/companies/validate/**")
                        .and().header("X-Internal-Service")
                        .filters(f -> f.filter(internalServiceAuthFilter.apply(
                                new InternalServiceAuthFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ================================================================================
                //                          USER SERVICE ROUTES
                // ================================================================================

                // ---------------------- PUBLIC ROUTES (No Authentication) ----------------------

                .route("auth-public-login", r -> r
                        .path("/api/auth/login")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                .route("auth-public-verify-email", r -> r
                        .path("/api/auth/verify-email")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                .route("auth-public-resend-otp", r -> r
                        .path("/api/auth/resend-otp")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                .route("auth-public-forgot-password", r -> r
                        .path("/api/auth/forgot-password")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                .route("auth-public-reset-password", r -> r
                        .path("/api/auth/reset-password")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                .route("auth-public-refresh-token", r -> r
                        .path("/api/auth/refresh-token")
                        .and().method("POST")
                        .uri("lb://USER-SERVICE2"))

                // ---------------------- ADMIN ONLY ROUTES ----------------------

                .route("auth-register-admin-only", r -> r
                        .path("/api/auth/register")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://USER-SERVICE2"))

                .route("users-getall-admin-only", r -> r
                        .path("/api/users")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://USER-SERVICE2"))

                // ---------------------- ADMIN & MANAGER ROUTES ----------------------

                .route("user-lock-admin-manager", r -> r
                        .path("/api/users/{userId}/lock")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-unlock-admin-manager", r -> r
                        .path("/api/users/{userId}/unlock")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-delete-admin-manager", r -> r
                        .path("/api/users/{userId}")
                        .and().method("DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-restore-admin-manager", r -> r
                        .path("/api/users/{userId}/restore")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                // ---------------------- ADMIN, MANAGER & PROVIDER_MANAGER ROUTES ----------------------

                .route("user-update-shifts", r -> r
                        .path("/api/users/pm")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-oncall-status", r -> r
                        .path("/api/users/onCall")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-se-by-company", r -> r
                        .path("/api/users/details/seByCompanyId/{compId}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-available-se-by-company", r -> r
                        .path("/api/users/details/avlSeByCompanyId/{compId}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                .route("user-busy-se-by-company", r -> r
                        .path("/api/users/details/busySeByCompanyId/{compId}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://USER-SERVICE2"))

                // ---------------------- AUTHENTICATED ROUTES (All Roles) ----------------------

                .route("auth-change-password", r -> r
                        .path("/api/auth/change-password")
                        .and().method("POST")
                        .filters(f -> f.filter(
                                authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-profile-authenticated", r -> r
                        .path("/api/users/profile")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-by-id-authenticated", r -> r
                        .path("/api/users/{userId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-public-by-id", r -> r
                        .path("/api/users/public/{userId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-public-by-employee-id", r -> r
                        .path("/api/users/public/employeeId/{employeeId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-by-company-auto", r -> r
                        .path("/api/users/public/company/auto")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-by-company-id", r -> r
                        .path("/api/users/public/company/{compId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-get-name", r -> r
                        .path("/api/users/getName/{userId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-pm-by-id", r -> r
                        .path("/api/users/details/pmById/{userId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-pm-by-company", r -> r
                        .path("/api/users/details/pmByCompanyId/{compId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                .route("user-authenticated-fallback", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://USER-SERVICE2"))

                // ================================================================================
                //                       INDEPENDENT SERVICE ROUTES
                // ================================================================================

                // ---------------------- PUBLIC ROUTES (No Authentication) ----------------------

                .route("roles-public", r -> r
                        .path("/api/roles")
                        .and().method("GET")
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("companies-public-active", r -> r
                        .path("/api/companies/active")
                        .and().method("GET")
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------------------- ADMIN ONLY ROUTES ----------------------

                .route("roles-write-admin-only", r -> r
                        .path("/api/roles/{role}")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("companies-write-admin-only", r -> r
                        .path("/api/companies/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("priority-write-admin-only", r -> r
                        .path("/api/priority/**")
                        .and().method("POST", "DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------------------- ADMIN & MANAGER ROUTES ----------------------

                .route("services-write-admin-manager", r -> r
                        .path("/api/services/**")
                        .and().method("POST", "DELETE", "PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("services-getall-admin-manager", r -> r
                        .path("/api/services")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("companies-read-admin-manager", r -> r
                        .path("/api/companies", "/api/companies/deleted")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("companies-getbyid-admin-manager", r -> r
                        .path("/api/companies/getById/{id}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("companies-client-provider-admin-manager", r -> r
                        .path("/api/companies/clientCompany", "/api/companies/providerCompany")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ---------------------- AUTHENTICATED ROUTES (All Roles) ----------------------

                .route("services-read-authenticated", r -> r
                        .path("/api/services/**")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("priority-read-authenticated", r -> r
                        .path("/api/priority", "/api/priority/*")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                .route("validate-authenticated", r -> r
                        .path("/api/roles/validate/**", "/api/companies/validate/**")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INDEPENDENT-SERVICES"))

                // ================================================================================
                //                            SLA SERVICE ROUTES
                // ================================================================================

                // ---------------------- ADMIN ONLY ROUTES ----------------------

                .route("sla-delete-admin-only", r -> r
                        .path("/api/sla/**")
                        .and().method("DELETE")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://SLA-SERVICE"))

                // ---------------------- ADMIN & MANAGER ROUTES ----------------------

                .route("sla-write-admin-manager", r -> r
                        .path("/api/sla", "/api/sla/rule/**")
                        .and().method("POST", "PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://SLA-SERVICE"))

                .route("sla-getall-admin-manager", r -> r
                        .path("/api/sla")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://SLA-SERVICE"))

                // ---------------------- ADMIN, MANAGER, PROVIDER_MANAGER & REPORTER ROUTES ----------------------

                .route("sla-read-by-service-priority", r -> r
                        .path("/api/sla/service/**", "/api/sla/priority/**")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config(
                                                "ADMIN", "MANAGER", "PROVIDER_MANAGER", "REPORTER"
                                        ))))
                        .uri("lb://SLA-SERVICE"))

                // ---------------------- ADMIN, MANAGER, REPORTER & SUPPORT_ENGINEER ROUTES ----------------------

                .route("sla-rule-read-authenticated", r -> r
                        .path("/api/sla/rule/**")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config(
                                                "ADMIN", "MANAGER", "REPORTER", "PROVIDER_MANAGER", "SUPPORT_ENGINEER"
                                        ))))
                        .uri("lb://SLA-SERVICE"))

                // ================================================================================
                //                         INCIDENT SERVICE ROUTES
                // ================================================================================

                // ---------------------- ADMIN ONLY ROUTES ----------------------

                .route("incident-getall-admin-only", r -> r
                        .path("/api/incidents")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN"))))
                        .uri("lb://INCIDENT-SERVICE"))

                // ---------------------- ADMIN & REPORTER ROUTES ----------------------

                .route("incident-create-admin-reporter", r -> r
                        .path("/api/incidents")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "REPORTER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                // ---------------------- ADMIN, MANAGER & PROVIDER_MANAGER ROUTES ----------------------

                .route("incident-pm-allocation", r -> r
                        .path("/api/incidents/providerManager/{incidentId}")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-assign-se", r -> r
                        .path("/api/incidents/assign")
                        .and().method("POST")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-sla-status-admin-manager", r -> r
                        .path("/api/incidentSla/{status}")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-get-status-authenticated", r -> r
                        .path("/api/incidents/getStatus")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-status-update", r -> r
                        .path("/api/incidents/statusUpdate")
                        .and().method("PUT")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "PROVIDER_MANAGER", "SUPPORT_ENGINEER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-my-incidents", r -> r
                        .path("/api/incidents/getMyIncidents")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("incident-precise-response", r -> r
                        .path("/api/incidents/preciseResponse/{compId}")
                        .and().method("GET")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("comments-getall-admin-manager", r -> r
                        .path("/api/comments")
                        .and().method("GET")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                // ---------------------- AUTHENTICATED ROUTES ----------------------

                .route("incident-authenticated-routes", r -> r
                        .path("/api/incidents/**")
                        .filters(f -> f.filter(authenticationFilter.apply(
                                new AuthenticationFilter.Config())))
                        .uri("lb://INCIDENT-SERVICE"))

                .route("comments-authenticated-routes", r -> r
                        .path("/api/comments/**")
                        .and().method("GET", "POST")
                        .filters(f -> f
                                .filter(authenticationFilter.apply(new AuthenticationFilter.Config()))
                                .filter(roleBasedAuthorizationFilter.apply(
                                        new RoleBasedAuthorizationFilter.Config("ADMIN", "MANAGER", "REPORTER", "SUPPORT_ENGINEER", "PROVIDER_MANAGER"))))
                        .uri("lb://INCIDENT-SERVICE"))

                .build();
    }
}