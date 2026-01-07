package com.trackIt.incident_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trackIt.incident_service.model.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncidentResponse {

    private Long incidentId;
    private String title;
    private String description;
    private Long serviceId;
    private String priority;
    private Status status;
    private String reportedBy;
    private String assignedTo;
    private String reportedAt;
    private String resolvedAt;
    private String createdAt;
    private String updatedAt;

}
