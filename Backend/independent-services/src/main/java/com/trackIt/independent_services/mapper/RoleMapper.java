package com.trackIt.independent_services.mapper;

import com.trackIt.independent_services.dto.RolesResponse;
import com.trackIt.independent_services.model.Roles;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String sanitizeRole(String role) {
        if (role == null || role.isBlank()) {
            return null;
        }

        return role.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9_]", "")
                .toUpperCase();
    }

    public static List<RolesResponse> toResponseList(List<Roles> roles) {
        if (roles == null || roles.isEmpty()){
            return List.of();
        }
        return roles.stream()
                .map(RoleMapper::toResponse)
                .collect(Collectors.toList());
    }

    public static RolesResponse toResponse(Roles role) {
        if(role == null){
            return null;
        }

        return RolesResponse.builder()
                .roleId(role.getRoleId())
                .role(role.getRole())
                .createdAt(role.getCreatedAt() != null ? role.getCreatedAt().format(FORMATTER) : null)
                .build();
    }
}
