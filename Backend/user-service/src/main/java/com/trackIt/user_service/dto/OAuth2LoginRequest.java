package com.trackIt.user_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2LoginRequest {
    @NotBlank(message = "ID token is required")
    private String idToken;

    private Long roleId;
    private Long companyId;
}
