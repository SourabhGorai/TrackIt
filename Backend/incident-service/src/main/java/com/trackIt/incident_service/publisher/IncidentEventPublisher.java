package com.trackIt.incident_service.publisher;

import com.trackIt.incident_service.constants.KafkaTopics;
import com.trackIt.incident_service.dto.IncidentCreatedEvent;
import com.trackIt.incident_service.dto.IncidentStatusChangedEvent;
import com.trackIt.incident_service.dto.SupportEngineerAssignedEvent;
import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.model.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public void publishIncidentCreated(
            Incident incident,
            String priority,
            Long compId,
            String companyName,
            Long serviceId,
            String serviceName
    ) {

        IncidentCreatedEvent event = IncidentCreatedEvent.builder()
                .eventType("INCIDENT_CREATED")
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .serviceId(serviceId)
                .serviceName(serviceName)
                .priority(priority)
                .providerCompId(compId)
                .clientCompanyName(companyName)
                .reportedAt(format(incident.getReportedAt()))
                .build();

        sendEvent(KafkaTopics.INCIDENT_EVENTS, incident.getIncidentId().toString(), event);

        log.info("Published INCIDENT_CREATED event for incidentId={}",
                incident.getIncidentId());
    }

    public void publishSupportEngineerAssigned(
            Incident incident,
            Status previousStatus,
            String priority,
            String serviceName,
            Long reporterId,
            Long providerManagerId,
            Long supportEngineerId,
            String supportEngineerName,
            String supportEngineerEmployeeId
    ) {
        // Build list of user IDs to notify
        List<Long> notifyUserIds = new ArrayList<>();

        if (reporterId != null) {
            notifyUserIds.add(reporterId);
        }
        if (providerManagerId != null) {
            notifyUserIds.add(providerManagerId);
        }
        if (supportEngineerId != null) {
            notifyUserIds.add(supportEngineerId);
        }

        SupportEngineerAssignedEvent event = SupportEngineerAssignedEvent.builder()
                .eventType("SUPPORT_ENGINEER_ASSIGNED")
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .serviceName(serviceName)
                .priority(priority)
                .supportEngineerId(supportEngineerId)
                .supportEngineerName(supportEngineerName)
                .supportEngineerEmployeeId(supportEngineerEmployeeId)
                .previousStatus(previousStatus != null ? previousStatus.name() : null)
                .newStatus(incident.getStatus().name())
                .notifyUserIds(notifyUserIds)
                .assignedAt(format(incident.getUpdatedAt()))
                .build();

        sendEvent(KafkaTopics.INCIDENT_EVENTS, incident.getIncidentId().toString(), event);

        log.info("Published SUPPORT_ENGINEER_ASSIGNED event for incidentId={}, assignedTo={}",
                incident.getIncidentId(), supportEngineerName);
    }

    public void publishIncidentStatusChanged(
            Incident incident,
            Status newStatus,
            Status previousStatus,
            String priority,
            String serviceName,
            Long reporterId,
            Long providerManagerId,
            Long supportEngineerId
    ) {
        // Build list of user IDs to notify
        List<Long> notifyUserIds = new ArrayList<>();

        if (reporterId != null) {
            notifyUserIds.add(reporterId);
        }
        if (providerManagerId != null) {
            notifyUserIds.add(providerManagerId);
        }
        if (supportEngineerId != null) {
            notifyUserIds.add(supportEngineerId);
        }

        IncidentStatusChangedEvent event = IncidentStatusChangedEvent.builder()
                .eventType("INCIDENT_STATUS_CHANGED")
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .serviceName(serviceName)
                .priority(priority)
                .previousStatus(previousStatus != null ? previousStatus.name() : null)
                .newStatus(newStatus.name())
                .notifyUserIds(notifyUserIds)
                .updatedAt(format(incident.getUpdatedAt()))
                .resolvedAt(newStatus == Status.RESOLVED ? format(incident.getResolvedAt()) : null)
                .build();

        sendEvent(KafkaTopics.INCIDENT_EVENTS, incident.getIncidentId().toString(), event);

        log.info("Published INCIDENT_STATUS_CHANGED event for incidentId={}, status changed from {} to {}",
                incident.getIncidentId(), previousStatus, newStatus);
    }

    private void sendEvent(String topic, String key, Object event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, key, event);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send event to Kafka. Topic: {}, Key: {}, Event: {}",
                            topic, key, event, ex);
                } else {
                    log.debug("Successfully sent event to Kafka. Topic: {}, Partition: {}, Offset: {}",
                            topic,
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("Exception while sending event to Kafka. Topic: {}, Key: {}",
                    topic, key, e);
        }
    }

    private static String format(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : null;
    }
}