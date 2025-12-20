package com.trackIt.user_service.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Company ID is required")
    private String employeeId;

    @NotBlank(message = "Password is required")
    private String password;
}