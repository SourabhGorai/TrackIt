package com.trackIt.incidents.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IncidentSummary {

    private String status;
    private Long count;
}
