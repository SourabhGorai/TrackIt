// SlaRulesRequest.java
package com.trackIt.sla_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlaRulesRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Priority ID is required")
    private Long priorityId;

    @NotNull(message = "Response time is required")
    @Positive(message = "Response time must be positive")
    private Integer response_time_mins;

    @NotNull(message = "Resolution time is required")
    @Positive(message = "Resolution time must be positive")
    private Integer resolution_time_mins;

}