package com.trackIt.incidents.mapper;


import com.trackIt.incidents.client.IndependentServiceClient;
import com.trackIt.incidents.client.UserServiceClient;
import com.trackIt.incidents.dto.request.ReporterRequest;
import com.trackIt.incidents.dto.response.IncidentResponse;
import com.trackIt.incidents.dto.response.PriorityResponse;
import com.trackIt.incidents.dto.response.UserResponsePublic;
import com.trackIt.incidents.model.Incident;
import com.trackIt.incidents.model.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;

@Component
public class IncidentMapper {

    private static final Pattern TITLE_CASE_PATTERN = Pattern.compile("\\b[a-z]");
    private static final Pattern SENTENCE_CASE_PATTERN = Pattern.compile("^[a-z]");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String sanitizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return title;
        }

        // Remove special characters except spaces
        String cleaned = title
                .replaceAll("[^a-zA-Z0-9 ]+", " ")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();

        // Convert to Title Case
        Matcher matcher = TITLE_CASE_PATTERN.matcher(cleaned);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(result, matcher.group().toUpperCase());
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static String sanitizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return description;
        }

        String cleaned = description
                .trim()
                .replaceAll("\\s+", " ");

        // Capitalize only the first letter
        Matcher matcher = SENTENCE_CASE_PATTERN.matcher(cleaned);
        return matcher.find()
                ? matcher.replaceFirst(matcher.group().toUpperCase())
                : cleaned;
    }

    public static String sanitizeName(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            return null;
        }

        return companyName.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-zA-Z0-9]", "")
                .toUpperCase();
    }

    public static Incident reporterRequestToIncident(ReporterRequest req, Long userId) {
        if(req == null) return null;

        return Incident.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .serviceId(req.getServiceId())
                .status(Status.OPEN)
                .reportedBy(userId)
                .assignedTo(null)
                .reportedAt(LocalDateTime.now())
                .resolvedAt(null)
                .build();
    }

    public static List<IncidentResponse> toResponseList(
            List<Incident> incidents,
            UserServiceClient userServiceClient,
            IndependentServiceClient independentServiceClient
    ) {

        if (incidents == null || incidents.isEmpty()) {
            return List.of();
        }

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

                    return IncidentMapper.toResponse(
                            incident,
                            reporterName,
                            priority,
                            assignedName
                    );
                })
                .toList();
    }


    public static IncidentResponse toResponse(Incident saved, String reporterName,
                                              String priority, String assigned) {

        if(saved == null) return null;

        return IncidentResponse.builder()
                .incidentId(saved.getIncidentId())
                .title(saved.getTitle())
                .description(saved.getDescription())
                .serviceId(saved.getServiceId())
                .priority(priority)
                .status(saved.getStatus())
                .reportedBy(reporterName)
                .assignedTo(assigned)
                .reportedAt(
                        saved.getReportedAt() != null
                                ? saved.getReportedAt().format(FORMATTER)
                                : null
                )
                .resolvedAt(
                        saved.getResolvedAt() != null
                                ? saved.getResolvedAt().format(FORMATTER)
                                : null
                )
                .createdAt(
                        saved.getCreatedAt() != null
                                ? saved.getCreatedAt().format(FORMATTER)
                                : null
                )
                .updatedAt(
                        saved.getUpdatedAt() != null
                                ? saved.getUpdatedAt().format(FORMATTER)
                                : null
                )
                .build();

    }
}
