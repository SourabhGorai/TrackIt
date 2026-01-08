package com.trackIt.notification_service.service;

import com.trackIt.notification_service.dto.*;
import com.trackIt.notification_service.external.UserServiceClient;
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
        List<ProviderManagerFullResponse> managers =
                userServiceClient.getProviderManagersByCompanyId(
                        event.getProviderCompId()
                );

        if (managers == null || managers.isEmpty()) {
            log.warn("No Provider Managers found for company ID: {}",
                    event.getProviderCompId());
            return;
        }

        for (ProviderManagerFullResponse pm : managers) {

            String message = buildIncidentCreatedMessage(event, pm.getName());

            notificationSender.send(
                    pm.getEmail(),
                    "New Incident Reported",
                    message
            );
        }
    }

    public void notifyStatusChange(IncidentStatusChangedEvent event) {

        log.info("Notifying users for status change: incidentId={}, newStatus={}",
                event.getIncidentId(), event.getNewStatus());

        List<Long> userIds = event.getNotifyUserIds();

        if (userIds == null || userIds.isEmpty()) {
            log.warn("No users to notify for incident ID: {}", event.getIncidentId());
            return;
        }

        for (Long userId : userIds) {
            try {
                UserResponsePublic user = userServiceClient.getUserDetails(userId);

                if (user == null) {
                    log.warn("User not found with ID: {}", userId);
                    continue;
                }

                String subject = getSubjectForStatus(event.getNewStatus(), event.getIncidentId());
                String message = buildStatusChangeMessage(event, user.getName());

                notificationSender.send(
                        user.getEmail(),
                        subject,
                        message
                );

                log.info("Sent status change notification to user: {} ({})",
                        user.getName(), user.getEmail());

            } catch (Exception e) {
                log.error("Failed to notify user ID: {}", userId, e);
            }
        }
    }

    public void notifySupportEngineerAssigned(SupportEngineerAssignedEvent event) {

        log.info("Notifying users about support engineer assignment: incidentId={}, engineer={}",
                event.getIncidentId(), event.getSupportEngineerName());

        List<Long> userIds = event.getNotifyUserIds();

        if (userIds == null || userIds.isEmpty()) {
            log.warn("No users to notify for incident ID: {}", event.getIncidentId());
            return;
        }

        for (Long userId : userIds) {
            try {
                UserResponsePublic user = userServiceClient.getUserDetails(userId);

                if (user == null) {
                    log.warn("User not found with ID: {}", userId);
                    continue;
                }

                String subject = String.format("Support Engineer Assigned to Incident #%d",
                        event.getIncidentId());
                String message = buildSupportEngineerAssignedMessage(event, user.getName(), userId);

                notificationSender.send(
                        user.getEmail(),
                        subject,
                        message
                );

                log.info("Sent support engineer assignment notification to user: {} ({})",
                        user.getName(), user.getEmail());

            } catch (Exception e) {
                log.error("Failed to notify user ID: {} about support engineer assignment", userId, e);
            }
        }
    }

    private String buildSupportEngineerAssignedMessage(
            SupportEngineerAssignedEvent event,
            String recipientName,
            Long recipientId
    ) {
        StringBuilder message = new StringBuilder();
        message.append(String.format("Hello %s,\n\n", recipientName));

        // Check if the recipient is the support engineer themselves
        boolean isSupportEngineer = recipientId.equals(event.getSupportEngineerId());

        if (isSupportEngineer) {
            message.append(String.format("You have been assigned to Incident #%d.\n\n",
                    event.getIncidentId()));
        } else {
            message.append(String.format("A support engineer has been assigned to Incident #%d.\n\n",
                    event.getIncidentId()));
        }

        message.append("Incident Details:\n");
        message.append(String.format("Title: %s\n", event.getTitle()));
        message.append(String.format("Service: %s\n", event.getServiceName()));
        message.append(String.format("Priority: %s\n", event.getPriority()));

        if (event.getPreviousStatus() != null) {
            message.append(String.format("Previous Status: %s\n", event.getPreviousStatus()));
        }

        message.append(String.format("Current Status: %s\n", event.getNewStatus()));
        message.append(String.format("Assigned At: %s\n\n", event.getAssignedAt()));

        message.append("Support Engineer Details:\n");
        message.append(String.format("Name: %s\n", event.getSupportEngineerName()));
        message.append(String.format("Employee ID: %s\n\n", event.getSupportEngineerEmployeeId()));

        // Add role-specific messages
        if (isSupportEngineer) {
            message.append("Action Required:\n");
            message.append("• Review the incident details in your dashboard\n");
            message.append("• Update the status as you make progress\n");
            message.append("• Contact the reporter if you need additional information\n");
            message.append("• Aim to resolve this incident according to the SLA guidelines\n");
        } else {
            message.append("The support engineer will begin working on this incident shortly.\n");
            message.append("You will receive updates as the incident progresses.\n");
            message.append("\nIf you have any questions, please contact the support team.\n");
        }

        message.append("\nRegards,\n");
        message.append("TrackIt System");

        return message.toString();
    }

    private String buildIncidentCreatedMessage(IncidentCreatedEvent event, String name) {

        return """
                Hello %s,

                A new incident has been reported.

                Incident ID: %d
                Title: %s
                Service: %d - %s
                Company: %s
                Priority: %s
                Reported At: %s

                Please accept the incident from your dashboard.

                Regards,
                TrackIt System
                """.formatted(
                name,
                event.getIncidentId(),
                event.getTitle(),
                event.getServiceId(),
                event.getServiceName(),
                event.getClientCompanyName(),
                event.getPriority(),
                event.getReportedAt()
        );
    }

    private String buildStatusChangeMessage(IncidentStatusChangedEvent event, String name) {

        StringBuilder message = new StringBuilder();
        message.append(String.format("Hello %s,\n\n", name));
        message.append(String.format("The status of Incident #%d has been updated.\n\n",
                event.getIncidentId()));
        message.append("Details:\n");
        message.append(String.format("Title: %s\n", event.getTitle()));
        message.append(String.format("Service: %s\n", event.getServiceName()));
        message.append(String.format("Priority: %s\n", event.getPriority()));

        if (event.getPreviousStatus() != null) {
            message.append(String.format("Previous Status: %s\n", event.getPreviousStatus()));
        }

        message.append(String.format("New Status: %s\n", event.getNewStatus()));
        message.append(String.format("Updated At: %s\n\n", event.getUpdatedAt()));

        // Add specific messages based on status
        switch (event.getNewStatus()) {
            case "RESOLVED" -> {
                message.append("✓ This incident has been resolved.\n");
                if (event.getResolvedAt() != null) {
                    message.append(String.format("Resolved At: %s\n", event.getResolvedAt()));
                }
                message.append("\nIf you're satisfied with the resolution, no further action is needed.\n");
                message.append("If the issue persists, please create a new incident or contact support.\n");
            }
            case "IN_PROGRESS" -> {
                message.append("Our team is actively working on resolving this incident.\n");
                message.append("You will be notified once there are further updates.\n");
            }
            case "ON_HOLD" -> {
                message.append("This incident has been placed on hold.\n");
                message.append("We may need additional information to proceed.\n");
                message.append("Please check your incident details for any pending actions.\n");
            }
            case "CLOSED" -> {
                message.append("This incident has been closed.\n");
                message.append("Thank you for your patience.\n");
            }
            case "OPEN" -> {
                message.append("This incident has been reopened.\n");
                message.append("Our team will review it shortly.\n");
            }
            default -> message.append("Please check your dashboard for more details.\n");
        }

        message.append("\nRegards,\n");
        message.append("TrackIt System");

        return message.toString();
    }

    private String getSubjectForStatus(String status, Long incidentId) {
        return switch (status) {
            case "RESOLVED" -> String.format("Incident #%d Resolved", incidentId);
            case "IN_PROGRESS" -> String.format("Incident #%d In Progress", incidentId);
            case "ON_HOLD" -> String.format("Incident #%d On Hold", incidentId);
            case "CLOSED" -> String.format("Incident #%d Closed", incidentId);
            case "OPEN" -> String.format("Incident #%d Reopened", incidentId);
            default -> String.format("Incident #%d Status Update", incidentId);
        };
    }
}