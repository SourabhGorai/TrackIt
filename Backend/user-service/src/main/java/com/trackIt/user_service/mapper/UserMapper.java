package com.trackIt.user_service.mapper;

import com.trackIt.user_service.dto.UserResponse;
import com.trackIt.user_service.model.Users;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static UserResponse toResponse(Users user){

        if(user==null){
            return null;
        }

        return UserResponse.builder()
                .employeeId(user.getEmployeeId())
                .name(user.getName())
                .email(user.getEmail())
                .roleId(user.getRoleId())
                .companyId(user.getCompanyId())
                .createdAt(user.getCreatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(user.getUpdatedAt() != null ? user.getCreatedAt().format(FORMATTER) : null)
                .build();

    }

    public static List<UserResponse> toResponseList(List<Users> users){
        if(users==null || users.isEmpty()){
            return List.of();
        }

        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

}
