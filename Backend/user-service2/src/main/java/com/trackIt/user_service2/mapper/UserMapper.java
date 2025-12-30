package com.trackIt.user_service2.mapper;

import com.trackIt.user_service2.dto.UserResponse;
import com.trackIt.user_service2.dto.UserResponsePublic;
import com.trackIt.user_service2.model.Users;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class UserMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");



    public static UserResponse toResponse(Users user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .companyId(user.getCompanyId())
                .isEmailVerified(user.getIsEmailVerified())
                .isAccountLocked(user.getIsAccountLocked())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getUpdatedAt().format(FORMATTER) : null)
                .build();
    }

    public static UserResponse toResponseWithDetails(Users user, String roleName, String companyName) {
        UserResponse response = toResponse(user);
        if (response != null) {
            response.setRoleName(roleName);
            response.setCompanyName(companyName);
        }
        return response;
    }

    public static UserResponsePublic toResponseWithPublicView(Users user, String roleName, String companyName) {

        if (user == null) return null;

        return UserResponsePublic.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .roleName(roleName)
                .companyId(user.getCompanyId())
                .companyName(companyName)
                .build();

    }
}