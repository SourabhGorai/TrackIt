package com.trackIt.incidents.dto.response;

import com.trackIt.incidents.model.Status;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateIncidentStatus {

    @NotNull(message = "Incident ID is required")
    private Long incidentId;

    @NotNull(message = "Status is required")
    private Status status;
}
