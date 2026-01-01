package com.trackIt.user_service2.dto.response;

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
public class ProviderManagerResponse {

    private Long id;
    private String employeeId;
    private String employeeName;
    private String shiftStart;
    private String shiftEnd;
    private Boolean isActive;
    private Boolean onCall;

}
