package com.trackIt.user_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    private String employeeId;
    private String name;
    private String email;
    private Long roleId;
    private Long companyId;
    private boolean isAccountLocked;
    private String createdAt;
    private String updatedAt;
}
