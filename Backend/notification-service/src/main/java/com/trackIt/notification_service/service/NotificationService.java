package com.trackIt.notification_service.service;

import com.trackIt.notification_service.dto.IncidentCreatedEvent;
import com.trackIt.notification_service.external.UserServiceClient;
import com.trackIt.notification_service.external.dto.UserResponsePublic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserServiceClient userServiceClient;
    private final NotificationSender notificationSender;

    public void notifyProviderManagers(IncidentCreatedEvent event) {

        log.info("Notifying Provider Managers for incidentId={}",
                event.getIncidentId());

        // Fetch all PROVIDER_MANAGER(s) for company
        List<UserResponsePublic> managers =
                userServiceClient.getProviderManagersByCompanyName(
                        event.getCompanyName()
                );

        for (UserResponsePublic pm : managers) {

            String message = buildMessage(event, pm.getName());

            notificationSender.send(
                    pm.getEmail(),
                    "New Incident Reported",
                    message
            );
        }
    }

    private String buildMessage(IncidentCreatedEvent event, String name) {

        return """
                Hello %s,

                A new incident has been reported.

                Incident ID: %d
                Title: %s
                Service: %s
                Priority: %s
                Reported At: %s

                Please accept the incident from your dashboard.

                Regards,
                TrackIt System
                """.formatted(
                name,
                event.getIncidentId(),
                event.getTitle(),
                event.getServiceName(),
                event.getPriority(),
                event.getReportedAt()
        );
    }
}
