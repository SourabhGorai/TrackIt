package com.trackIt.incidents.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponsePublic {
    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private Long roleId;
    private String roleName;
    private Long companyId;
    private String companyName;
}
