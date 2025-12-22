package com.trackIt.api_gateway.config;

import com.trackIt.api_gateway.filter.AuthenticationFilter;
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

    @Autowired
    public GatewayConfig(AuthenticationFilter authenticationFilter,
                         RoleBasedAuthorizationFilter roleBasedAuthorizationFilter) {
        this.authenticationFilter = authenticationFilter;
        this.roleBasedAuthorizationFilter = roleBasedAuthorizationFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

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

                .build();
    }
}

//package com.trackIt.api_gateway.config;
//import com.trackIt.api_gateway.filter.AuthenticationFilter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * Gateway configuration class
// * Routes can be defined here programmatically or in application.yml
// * Currently using application.yml for route definitions
// */
//@Configuration
//public class GatewayConfig {
//
//    private final AuthenticationFilter authenticationFilter;
//
//    // Inject AuthenticationFilter
//    @Autowired
//    public GatewayConfig(AuthenticationFilter authenticationFilter) {
//        this.authenticationFilter = authenticationFilter;
//    }
//
//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//
//                // ---------- PUBLIC ----------
//                .route("user-auth", r -> r
//                        .path("/api/auth/**")
//                        .uri("lb://USER-SERVICE2"))
//
//                .route("public-roles", r -> r
//                        .path("/api/roles")
//                        .uri("lb://INDEPENDENT-SERVICES"))
//
//                .route("public-companies", r -> r
//                        .path("/api/companies/active")
//                        .uri("lb://INDEPENDENT-SERVICES"))
//
//                // ---------- PROTECTED ----------
//                .route("user-auth", r -> r
//                        .path("/api/auth/register")
//                        .uri("lb://USER-SERVICE2"))
//
//                .route("user-management", r -> r
//                        .path("/api/users/**")
//                        .filters(f -> f.filter(authenticationFilter.apply(
//                                new AuthenticationFilter.Config())))
//                        .uri("lb://USER-SERVICE2"))
//
//                .route("independent-protected", r -> r
//                        .path("/api/**")
//                        .filters(f -> f.filter(authenticationFilter.apply(
//                                new AuthenticationFilter.Config())))
//                        .uri("lb://INDEPENDENT-SERVICES"))
//
//                .build();
//    }
//
//
////    @Bean
////    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
////        return builder.routes()
////                // User Service - Auth endpoints (public)
////                .route("user-auth", r -> r
////                        .path("/api/auth/**")
////                        .uri("lb://USER-SERVICE2"))
////
////                .route("roles-auth", r -> r
////                        .path("/api/roles", "/api/companies")
////                        .uri("lb://INDEPENDENT-SERVICES"))
////
////                // User Service - User management (protected)
////                .route("user-management", r -> r
////                        .path("/api/users/**")
////                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
////                        .uri("lb://USER-SERVICE"))
////
////                .build();
////    }
//}