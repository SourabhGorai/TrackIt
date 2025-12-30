package com.trackIt.incidents.dto.request;

import com.trackIt.incidents.model.Status;
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
