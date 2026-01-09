package com.trackIt.incident_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidentStatusChangedEvent {

    private String eventType;
    private Long incidentId;
    private String title;
    private String serviceName;
    private String priority;
    private String previousStatus;
    private String newStatus;
    private List<Long> notifyUserIds;
    private String updatedAt;
    private String resolvedAt;
}