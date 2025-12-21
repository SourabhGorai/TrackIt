package com.trackIt.user_service2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ============= ROLE RESPONSE FROM INDEPENDENT SERVICE =============
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long roleId;
    private String role;
    private String createdAt;
}