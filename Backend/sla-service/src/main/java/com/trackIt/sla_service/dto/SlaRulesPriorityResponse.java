// SlaRulesPriorityResponse.java
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
public class SlaRulesPriorityResponse {

    private Long slaId;
    private Long slaRulesId;
    private Long serviceId;
    private Integer response_time_mins;
    private Integer resolution_time_mins;

}