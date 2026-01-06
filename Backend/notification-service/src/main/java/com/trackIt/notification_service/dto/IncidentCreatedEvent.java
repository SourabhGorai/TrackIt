package com.trackIt.notification_service.dto;

import lombok.Data;

@Data
public class IncidentCreatedEvent {

    private String eventType;      // INCIDENT_CREATED
    private Long incidentId;

    private String title;
    private String serviceName;
    private String priority;
    private String companyName;

    private String reportedAt;
}
