package com.trackIt.notification_service.dto;

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

    private String eventType;           // INCIDENT_STATUS_CHANGED
    private Long incidentId;
    private String title;
    private String serviceName;
    private String priority;
    private String previousStatus;
    private String newStatus;
    private List<Long> notifyUserIds;   // List of users to notify
    private String updatedAt;
    private String resolvedAt;          // Only present when status is RESOLVED
}
