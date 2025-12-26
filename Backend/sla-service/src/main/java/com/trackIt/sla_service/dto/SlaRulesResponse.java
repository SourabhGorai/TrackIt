// SlaRulesResponse.java
package com.trackIt.sla_service.dto;

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
public class SlaRulesResponse<T> {

    private Long slaId;
    private Long serviceId;
    private T priorityLevel;
    private Integer response_time_mins;
    private Integer resolution_time_mins;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;

}