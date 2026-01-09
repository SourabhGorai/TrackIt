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
public class SlaBreachEvent {
    private String eventType; // "SLA_RESPONSE_BREACH" or "SLA_RESOLUTION_BREACH"
    private Long incidentId;
    private String title;
    private String serviceName;
    private String priority;
    private String breachType; // "RESPONSE" or "RESOLUTION"
    private String deadline;
    private String actualTime;
    private Integer minutesOverdue;
    private List<Long> notifyUserIds; // reporter, manager, assigned engineer
    private String breachedAt;
}