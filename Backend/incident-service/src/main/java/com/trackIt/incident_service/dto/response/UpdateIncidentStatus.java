package com.trackIt.incident_service.dto.response;

import com.trackIt.incident_service.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIncidentStatus {

    @NotNull(message = "Incident ID is required")
    private Long incidentId;

    @NotNull(message = "Status is required")
    private Status status;
}
