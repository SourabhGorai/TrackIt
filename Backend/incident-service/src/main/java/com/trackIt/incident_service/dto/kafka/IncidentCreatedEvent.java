package com.trackIt.incident_service.dto.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IncidentCreatedEvent {

    private String eventType;      // INCIDENT_CREATED
    private Long incidentId;

    private String title;
    private Long serviceId;
    private String serviceName;
    private String priority;
    private Long providerCompId;
    private String clientCompanyName;

    private String reportedAt;
}
