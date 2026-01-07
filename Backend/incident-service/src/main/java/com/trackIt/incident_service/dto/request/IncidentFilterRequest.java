package com.trackIt.incident_service.dto.request;

import com.trackIt.incident_service.model.Status;
import lombok.Data;

@Data
public class IncidentFilterRequest {

    private Long serviceId;
    private Long priorityId;
    private Status status;

    private Long reportedBy;
    private Long assignedTo;

    private String clientCompany;
    private String providerCompany;
}
