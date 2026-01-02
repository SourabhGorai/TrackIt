package com.trackIt.user_service2.mapper;

import com.trackIt.user_service2.dto.response.*;
import com.trackIt.user_service2.model.ProviderManagers;
import com.trackIt.user_service2.model.Users;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class UserMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a");



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


    public static ProviderManagerFullResponse toResponseFullPmInfo(
            Users user, CompanyResponse company, RoleResponse role, ProviderManagers pm
    ) {

        if(user == null || pm == null) return null;

        return ProviderManagerFullResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .roleName(role.getRole())
                .companyName(company.getCompanyName())
                .isEmailVerified(user.getIsEmailVerified())
                .isAccountLocked(user.getIsAccountLocked())
                .shiftStart(
                        pm.getShiftStart() != null
                                ? pm.getShiftStart().format(TIME_FORMATTER)
                                : null
                )
                .shiftEnd(
                        pm.getShiftEnd() != null
                                ? pm.getShiftEnd().format(TIME_FORMATTER)
                                : null
                )
                .isActive(!user.getIsDeleted())
                .onCall(pm.getOnCall())
                .build();

    }
}