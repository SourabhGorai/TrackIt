package com.trackIt.notification_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportEngineerAssignedEvent {
    private String eventType;
    private Long incidentId;
    private String title;
    private String serviceName;
    private String priority;
    private Long supportEngineerId;
    private String supportEngineerName;
    private String supportEngineerEmployeeId;
    private String previousStatus;
    private String newStatus;
    private List<Long> notifyUserIds;
    private String assignedAt;
}