package com.trackIt.notification_service.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackIt.notification_service.dto.*;
import com.trackIt.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "incident-events",
            groupId = "notification-service"
    )
    public void consumeIncidentEvents(Map<String, Object> eventMap) {

        try {
            String eventType = (String) eventMap.get("eventType");
            log.info("Received event: type={}, incidentId={}",
                    eventType, eventMap.get("incidentId"));

            switch (eventType) {
                case "INCIDENT_CREATED" -> handleIncidentCreated(eventMap);
                case "INCIDENT_STATUS_CHANGED" -> handleStatusChanged(eventMap);
                case "SUPPORT_ENGINEER_ASSIGNED" -> handleSupportEngineerAssigned(eventMap);
                case "SLA_RESPONSE_WARNING" -> handleSlaWarning(eventMap);
                case "SLA_RESOLUTION_WARNING" -> handleSlaWarning(eventMap);
                case "SLA_RESPONSE_BREACH" -> handleSlaBreach(eventMap);
                case "SLA_RESOLUTION_BREACH" -> handleSlaBreach(eventMap);
                default -> log.warn("Unknown event type: {}", eventType);
            }

        } catch (Exception e) {
            log.error("Error processing event", e);
        }
    }

    private void handleIncidentCreated(Map<String, Object> eventMap) {
        try {
            IncidentCreatedEvent event = objectMapper.convertValue(
                    eventMap,
                    IncidentCreatedEvent.class
            );

            log.info("Processing INCIDENT_CREATED for incidentId={}",
                    event.getIncidentId());

            notificationService.notifyProviderManagers(event);

        } catch (Exception e) {
            log.error("Error handling INCIDENT_CREATED event", e);
        }
    }

    private void handleStatusChanged(Map<String, Object> eventMap) {
        try {
            IncidentStatusChangedEvent event = objectMapper.convertValue(
                    eventMap,
                    IncidentStatusChangedEvent.class
            );

            log.info("Processing INCIDENT_STATUS_CHANGED for incidentId={}, status={}",
                    event.getIncidentId(), event.getNewStatus());

            notificationService.notifyStatusChange(event);

        } catch (Exception e) {
            log.error("Error handling INCIDENT_STATUS_CHANGED event", e);
        }
    }

    private void handleSupportEngineerAssigned(Map<String, Object> eventMap) {
        try {
            SupportEngineerAssignedEvent event = objectMapper.convertValue(
                    eventMap,
                    SupportEngineerAssignedEvent.class
            );

            log.info("Processing SUPPORT_ENGINEER_ASSIGNED for incidentId={}, engineer={}",
                    event.getIncidentId(), event.getSupportEngineerName());

            notificationService.notifySupportEngineerAssigned(event);

        } catch (Exception e) {
            log.error("Error handling SUPPORT_ENGINEER_ASSIGNED event", e);
        }
    }

    private void handleSlaWarning(Map<String, Object> eventMap) {
        try {
            SlaWarningEvent event = objectMapper.convertValue(
                    eventMap,
                    SlaWarningEvent.class
            );

            log.info("Processing {} for incidentId={}, {} minutes remaining",
                    event.getEventType(),
                    event.getIncidentId(),
                    event.getMinutesRemaining());

            notificationService.notifySlaWarning(event);

        } catch (Exception e) {
            log.error("Error handling SLA_WARNING event", e);
        }
    }

    private void handleSlaBreach(Map<String, Object> eventMap) {
        try {
            SlaBreachEvent event = objectMapper.convertValue(
                    eventMap,
                    SlaBreachEvent.class
            );

            log.info("Processing {} for incidentId={}, {} minutes overdue",
                    event.getEventType(),
                    event.getIncidentId(),
                    event.getMinutesOverdue());

            notificationService.notifySlaBreach(event);

        } catch (Exception e) {
            log.error("Error handling SLA_BREACH event", e);
        }
    }
}