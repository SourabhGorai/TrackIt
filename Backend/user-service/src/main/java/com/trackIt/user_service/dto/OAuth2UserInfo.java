package com.trackIt.user_service.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OAuth2UserInfo {

    private String email;
    private String name;
    private String provider; // GOOGLE, FACEBOOK, GITHUB
    private String providerId;
}