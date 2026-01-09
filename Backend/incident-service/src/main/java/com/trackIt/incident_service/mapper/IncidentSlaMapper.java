package com.trackIt.incident_service.mapper;

import com.trackIt.incident_service.dto.response.IncidentSlaResponse;
import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.model.IncidentSla;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IncidentSlaMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<IncidentSlaResponse> toResponseList(List<IncidentSla> incidentSlas){

        if(incidentSlas == null){
            return List.of();
        }

        return incidentSlas.stream()
                .map(IncidentSlaMapper::toResponse)
                .collect(Collectors.toList());

    }

    public static IncidentSlaResponse toResponse(IncidentSla incident) {

        if (incident == null) return null;

        return IncidentSlaResponse.builder()
                .incidentSlaId(incident.getIncidentSlaId())
                .incidentId(incident.getIncidentSlaId())
                .responseDeadLine(format(incident.getResponseDeadline()))
                .resolutionDeadLine(format(incident.getResolutionDeadline()))
                .respondAt(format(incident.getRespondedAt()))
                .resolveAt(format(incident.getResolvedAt()))
                .responseBreached(incident.isResponseBreached())
                .resolutionBreached(incident.isResolutionBreached())
                .isActive(incident.isActive())
                .build();

    }

    private static String format(LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : null;
    }


}
