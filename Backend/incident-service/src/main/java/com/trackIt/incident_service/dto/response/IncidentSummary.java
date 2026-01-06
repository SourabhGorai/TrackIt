package com.trackIt.incident_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IncidentSummary {

    private String status;
    private Long count;
}
