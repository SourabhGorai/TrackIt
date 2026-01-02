package com.trackIt.user_service2.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProviderManagerFullResponse {

    private Long id;
    private String employeeId;
    private String name;
    private String email;
    private String roleName;
    private String companyName;
    private Boolean isEmailVerified;
    private Boolean isAccountLocked;
    private String shiftStart;
    private String shiftEnd;
    private Boolean isActive;
    private Boolean onCall;
}
