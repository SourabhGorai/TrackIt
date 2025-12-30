package com.trackIt.incidents.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignSupportEngineerRequest {

    @NotNull(message = "Incident ID is required")
    private Long incidentId;

    @NotNull(message = "Assignee ID is required")
    private String assignedTo;          // It should be Employee ID
}
