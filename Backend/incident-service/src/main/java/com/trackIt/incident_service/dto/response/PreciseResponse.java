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
public class PreciseResponse {

    private Long incidentId;
    private String title;
    private String description;
    private Long serviceId;
    private String priorityLevel;
    private String expectedResponseTime;
    private String expectedResolutionTime;
    private Status status;
    private String reportedByEmpId;    // Each ID will be an employee ID
    private String reportedBy;
    private String managerAllocatedEmpId;
    private String managerAllocated;
    private String supporterAssignedEmpId;
    private String supporterAssigned;
    private String reportedAt;
    private String resolvedAt;

}
