package com.trackIt.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentCreatedEvent {

    private String eventType;           // INCIDENT_CREATED
    private Long incidentId;
    private String title;
    private Long serviceId;
    private String serviceName;
    private String priority;
    private Long providerCompId;
    private String clientCompanyName;
    private String reportedAt;
}