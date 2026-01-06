package com.trackIt.notificationservice.consumer;

import com.trackIt.notificationservice.dto.IncidentCreatedEvent;
import com.trackIt.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IncidentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "incident-events",
            groupId = "notification-service"
    )
    public void consumeIncidentEvents(IncidentCreatedEvent event) {

        log.info("Received event: {}", event);

        if (!"INCIDENT_CREATED".equals(event.getEventType())) {
            log.info("Ignoring eventType={}", event.getEventType());
            return;
        }

        notificationService.notifyProviderManagers(event);
    }
}
