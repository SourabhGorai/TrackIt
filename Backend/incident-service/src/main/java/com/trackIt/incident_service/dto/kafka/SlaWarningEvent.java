package com.trackIt.incident_service.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaWarningEvent {
    private String eventType; // "SLA_RESPONSE_WARNING" or "SLA_RESOLUTION_WARNING"
    private Long incidentId;
    private String title;
    private String serviceName;
    private String priority;
    private String warningType; // "RESPONSE" or "RESOLUTION"
    private Integer minutesRemaining;
    private String deadline;
    private List<Long> notifyUserIds; // reporter, manager, assigned engineer
    private String triggeredAt;
}