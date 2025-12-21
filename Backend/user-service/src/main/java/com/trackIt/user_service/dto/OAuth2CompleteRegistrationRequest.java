package com.trackIt.user_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2CompleteRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Provider is required")
    private String provider; // GOOGLE, FACEBOOK, GITHUB

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    @NotBlank(message = "Role ID is required")
    private Long roleId;

    @NotBlank(message = "Company ID is required")
    private Long companyId;
}