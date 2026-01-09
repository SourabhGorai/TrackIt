package com.trackIt.incident_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentSlaResponse {

    private Long incidentSlaId;
    private Long incidentId;
    private String responseDeadLine;
    private String resolutionDeadLine;
    private String respondAt;
    private String resolveAt;
    private Boolean responseBreached;
    private Boolean resolutionBreached;
    private Boolean isActive;

}
