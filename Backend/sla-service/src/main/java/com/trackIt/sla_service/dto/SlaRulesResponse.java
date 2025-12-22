package com.trackIt.sla_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlaRulesResponse<T> {

    private Long serviceId;
    private T priorityLevel;
    private int response_time_mins;
    private int resolution_time_mins;
    private String createdAt;
    private String updatedAt;
    private boolean isActive;
    
}
