package com.trackIt.incident_service.publisher;

import com.trackIt.incident_service.constants.KafkaTopics;
import com.trackIt.incident_service.dto.IncidentCreatedEvent;
import com.trackIt.incident_service.model.Incident;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentEventPublisher {

    private final KafkaTemplate<String, IncidentCreatedEvent> kafkaTemplate;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public void publishIncidentCreated(
            Incident incident,
            String priority,
            String companyName,
            String serviceName
    ) {

        IncidentCreatedEvent event = IncidentCreatedEvent.builder()
                .eventType("INCIDENT_CREATED")
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .serviceName(serviceName)
                .priority(priority)
                .companyName(companyName)
                .reportedAt(format(incident.getReportedAt()))
                .build();

        kafkaTemplate.send(
                KafkaTopics.INCIDENT_EVENTS,
                incident.getIncidentId().toString(), // KEY → ordering per incident
                event
        );

        log.info("Published INCIDENT_CREATED event for incidentId={}",
                incident.getIncidentId());
    }

    private static String format(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : null;
    }
}
