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
public class SlaRulesRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Priority ID is required")
    private Long priorityId;

    @NotNull(message = "Response time is required")
    private int response_time_mins;

    @NotNull(message = "Resolution time is required")
    private int resolution_time_mins;

}
