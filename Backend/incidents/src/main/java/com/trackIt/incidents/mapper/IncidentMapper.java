package com.trackIt.incidents.mapper;

import com.trackIt.incidents.client.IndependentServiceClient;
import com.trackIt.incidents.client.SlaServiceClient;
import com.trackIt.incidents.client.UserServiceClient;
import com.trackIt.incidents.dto.request.ReporterRequest;
import com.trackIt.incidents.dto.response.*;
import com.trackIt.incidents.model.Incident;
import com.trackIt.incidents.model.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class IncidentMapper {

    private static final Pattern TITLE_CASE_PATTERN = Pattern.compile("\\b[a-z]");
    private static final Pattern SENTENCE_CASE_PATTERN = Pattern.compile("^[a-z]");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    /* -------------------- Sanitizers -------------------- */

    public static String sanitizeTitle(String title) {
        if (title == null || title.isBlank()) return title;

        String cleaned = title
                .replaceAll("[^a-zA-Z0-9 ]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

        Matcher matcher = TITLE_CASE_PATTERN.matcher(cleaned);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().toUpperCase());
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String sanitizeDescription(String description) {
        if (description == null || description.isBlank()) return description;

        String cleaned = description.trim().replaceAll("\\s+", " ");
        Matcher matcher = SENTENCE_CASE_PATTERN.matcher(cleaned);

        return matcher.find()
                ? matcher.replaceFirst(matcher.group().toUpperCase())
                : cleaned;
    }

    public static String sanitizeName(String companyName) {
        if (companyName == null || companyName.isBlank()) return null;

        return companyName.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    /* -------------------- Request → Entity -------------------- */

    public static Incident reporterRequestToIncident(ReporterRequest req, Long userId) {
        if (req == null) return null;

        return Incident.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .serviceId(req.getServiceId())
                .priorityId(req.getPriorityId())
                .status(Status.OPEN)
                .reportedBy(userId)
                .assignedTo(null)
                .reportedAt(LocalDateTime.now())
                .resolvedAt(null)
                .build();
    }

    /* -------------------- Basic Response -------------------- */

    public static List<IncidentResponse> toResponseList(
            List<Incident> incidents,
            UserServiceClient userServiceClient,
            IndependentServiceClient independentServiceClient
    ) {

        if (incidents == null || incidents.isEmpty()) return List.of();

        return incidents.stream()
                .map(incident -> {

                    String reporterName = Optional
                            .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                            .map(UserResponsePublic::getName)
                            .orElse(null);

                    String assignedName = incident.getAssignedTo() != null
                            ? Optional.ofNullable(
                            userServiceClient.getUserDetails(incident.getAssignedTo())
                    ).map(UserResponsePublic::getName).orElse(null)
                            : null;

                    String priority = Optional
                            .ofNullable(
                                    independentServiceClient.validatePriority(incident.getPriorityId())
                            )
                            .map(PriorityResponse::getPriorityLevel)
                            .orElse(null);

                    return toResponse(incident, reporterName, priority, assignedName);
                })
                .toList();
    }

    public static IncidentResponse toResponse(
            Incident incident,
            String reporterName,
            String priority,
            String assigned
    ) {

        if (incident == null) return null;

        return IncidentResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .serviceId(incident.getServiceId())
                .priority(priority)
                .status(incident.getStatus())
                .reportedBy(reporterName)
                .assignedTo(assigned)
                .reportedAt(format(incident.getReportedAt()))
                .resolvedAt(format(incident.getResolvedAt()))
                .createdAt(format(incident.getCreatedAt()))
                .updatedAt(format(incident.getUpdatedAt()))
                .build();
    }

    /* -------------------- Precise Response -------------------- */


    public static List<PreciseResponse> toPreciseResponseList(
            List<Incident> incidents,
            UserServiceClient userServiceClient,
            IndependentServiceClient independentServiceClient,
            SlaServiceClient slaServiceClient
    ) {
        if (incidents == null || incidents.isEmpty()) return List.of();

        return incidents.stream()
                .map(incident -> {

                    PromiseResponse promise = slaServiceClient.getPromisedTimes(
                            incident.getServiceId(),
                            incident.getPriorityId()
                    );

                    return toPreciseResponse(
                            incident,
                            userServiceClient,
                            independentServiceClient,
                            promise
                    );
                })
                .toList();
    }


    public static PreciseResponse toPreciseResponse(
            Incident incident,
            UserServiceClient userServiceClient,
            IndependentServiceClient independentServiceClient,
            PromiseResponse promise
    ) {
        if (incident == null) return null;

        // Fetch reporter details
        UserResponsePublic reporter = Optional
                .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                .orElse(null);

        // Fetch manager details (assignedManagerId)
        UserResponsePublic manager = incident.getAssignedManagerId() != null
                ? Optional.ofNullable(
                userServiceClient.getUserDetails(incident.getAssignedManagerId())
        ).orElse(null)
                : null;

        // Fetch supporter details (assignedTo)
        UserResponsePublic supporter = incident.getAssignedTo() != null
                ? Optional.ofNullable(
                userServiceClient.getUserDetails(incident.getAssignedTo())
        ).orElse(null)
                : null;

        // Fetch priority level
        String priorityLevel = Optional
                .ofNullable(
                        independentServiceClient.validatePriority(incident.getPriorityId())
                )
                .map(PriorityResponse::getPriorityLevel)
                .orElse(null);

        // Format expected times from promise
        String expectedResponseTime = promise != null && promise.getResponse_time_mins() != null
                ? promise.getResponse_time_mins() + " mins"
                : null;

        String expectedResolutionTime = promise != null && promise.getResolution_time_mins() != null
                ? promise.getResolution_time_mins() + " mins"
                : null;

        return PreciseResponse.builder()
                .incidentId(incident.getIncidentId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .serviceId(incident.getServiceId())
                .priorityLevel(priorityLevel)
                .expectedResponseTime(expectedResponseTime)
                .expectedResolutionTime(expectedResolutionTime)
                .status(incident.getStatus())
                .reportedByEmpId(reporter != null ? reporter.getEmployeeId() : null)
                .reportedBy(reporter != null ? reporter.getName() : null)
                .managerAllocatedEmpId(manager != null ? manager.getEmployeeId() : null)
                .managerAllocated(manager != null ? manager.getName() : null)
                .supporterAssignedEmpId(supporter != null ? supporter.getEmployeeId() : null)
                .supporterAssigned(supporter != null ? supporter.getName() : null)
                .reportedAt(format(incident.getReportedAt()))
                .resolvedAt(format(incident.getResolvedAt()))
                .build();
    }

    /* -------------------- Format Time -------------------- */

    private static String format(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : null;
    }
}
