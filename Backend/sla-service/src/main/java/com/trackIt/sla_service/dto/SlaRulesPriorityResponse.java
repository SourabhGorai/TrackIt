package com.trackIt.sla_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SlaRulesPriorityResponse {

    private Long slaRulesId;
    private Long serviceId;
    private int response_time_mins;
    private int resolution_time_mins;

}
