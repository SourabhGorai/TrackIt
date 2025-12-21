package com.trackIt.user_service2.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private Long companyId;
    private String companyName;
    private Boolean isEmailVerified;
    private Boolean isAccountLocked;
    private String createdAt;
    private String updatedAt;
}
