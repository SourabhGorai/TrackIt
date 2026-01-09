package com.trackIt.incident_service.service;

import com.trackIt.incident_service.client.IndependentServiceClient;
import com.trackIt.incident_service.client.SlaServiceClient;
import com.trackIt.incident_service.dto.response.IncidentSlaResponse;
import com.trackIt.incident_service.dto.response.PromiseResponse;
import com.trackIt.incident_service.dto.response.PriorityResponse;
import com.trackIt.incident_service.dto.response.ServicesResponse;
import com.trackIt.incident_service.exception.ServiceException;
import com.trackIt.incident_service.mapper.IncidentMapper;
import com.trackIt.incident_service.mapper.IncidentSlaMapper;
import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.model.IncidentSla;
import com.trackIt.incident_service.model.SlaStatus;
import com.trackIt.incident_service.publisher.IncidentEventPublisher;
import com.trackIt.incident_service.repository.IncidentSlaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentSlaService {

    private final IncidentSlaRepository incidentSlaRepository;
    private final SlaServiceClient slaServiceClient;
    private final IncidentEventPublisher incidentEventPublisher;
    private final IndependentServiceClient independentServiceClient;
//    private final IncidentSlaMapper incidentSlaMapper;

    private static final int WARNING_MINUTES = 10;

    @Transactional
    public void createIncidentSla(Incident incident) {

        log.info("Attempting to create new Incident SLA for Incident ID: {}",
                incident.getIncidentId());

        PromiseResponse slaPromise = slaServiceClient.getPromisedTimes(
                incident.getServiceId(),
                incident.getPriorityId()
        );

        LocalDateTime reportedAt = incident.getReportedAt();

        IncidentSla incidentSla = IncidentSla.builder()
                .incident(incident)
                .responseDeadline(
                        reportedAt.plusMinutes(slaPromise.getResponse_time_mins())
                )
                .resolutionDeadline(
                        reportedAt.plusMinutes(slaPromise.getResolution_time_mins())
                )
                .respondedAt(null)
                .resolvedAt(null)
                .responseBreached(false)
                .resolutionBreached(false)
                .responseWarningPublished(false)
                .resolutionWarningPublished(false)
                .isActive(true)
                .build();

        incidentSlaRepository.save(incidentSla);

        log.info("Created SLA for Incident ID: {} with response deadline: {} and resolution deadline: {}",
                incident.getIncidentId(),
                incidentSla.getResponseDeadline(),
                incidentSla.getResolutionDeadline());
    }

    @Transactional
    public void setResponseTime(Incident incident) {

        log.info("Attempting to set response time for Incident ID: {}",
                incident.getIncidentId());

        IncidentSla incidentSla = incidentSlaRepository
                .findByIncident_IncidentIdAndIsActiveTrue(incident.getIncidentId())
                .orElseThrow(() -> new ServiceException(
                        "Active SLA not found for Incident ID: " + incident.getIncidentId()
                ));

        if (incidentSla.getRespondedAt() != null) {
            log.info("Response time already set for Incident ID: {}",
                    incident.getIncidentId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        incidentSla.setRespondedAt(now);

        if (now.isAfter(incidentSla.getResponseDeadline())) {
            incidentSla.setResponseBreached(true);
            log.warn("Response SLA breached for Incident ID: {}", incident.getIncidentId());
        }

        incidentSlaRepository.save(incidentSla);
    }

    @Transactional
    public void setRespondTime(Incident incident){

        log.info("Attempting to set resolution time for Incident ID: {}",
                incident.getIncidentId());

        IncidentSla incidentSla = incidentSlaRepository
                .findByIncident_IncidentIdAndIsActiveTrue(incident.getIncidentId())
                .orElseThrow(() -> new ServiceException(
                        "Active SLA not found for Incident ID: " + incident.getIncidentId()
                ));

        if(incidentSla.getResolvedAt() != null){
            log.info("Resolution time already set for Incident ID: {}",
                    incident.getIncidentId());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        incidentSla.setResolvedAt(now);
        incidentSla.setActive(false);

        if(now.isAfter(incidentSla.getResolutionDeadline())){
            incidentSla.setResolutionBreached(true);
            log.warn("Resolution SLA breached for Incident ID: {}", incident.getIncidentId());
        }

        incidentSlaRepository.save(incidentSla);
    }

    /**
     * Scheduled job to monitor SLA deadlines and publish warnings/breaches
     * Runs every 2 minutes
     */
    @Scheduled(fixedRate = 120000) // Run every 2 minutes
    @Transactional
    public void monitorSlaDeadlines() {
        log.info("Starting SLA monitoring job");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime warningThreshold = now.plusMinutes(WARNING_MINUTES);

            // Check for approaching response deadlines
            processResponseWarnings(now, warningThreshold);

            // Check for approaching resolution deadlines
            processResolutionWarnings(now, warningThreshold);

            // Check for response breaches
            processResponseBreaches(now);

            // Check for resolution breaches
            processResolutionBreaches(now);

            log.info("Completed SLA monitoring job");

        } catch (Exception e) {
            log.error("Error during SLA monitoring job", e);
        }
    }

    private void processResponseWarnings(LocalDateTime now, LocalDateTime warningThreshold) {
        List<IncidentSla> approachingResponse =
                incidentSlaRepository.findApproachingResponseDeadline(now, warningThreshold);

        log.info("Found {} incidents approaching response deadline", approachingResponse.size());

        for (IncidentSla sla : approachingResponse) {
            try {
                Incident incident = sla.getIncident();

                long minutesRemaining = ChronoUnit.MINUTES.between(now, sla.getResponseDeadline());

                // Fetch service and priority details
                ServicesResponse service = independentServiceClient.validateService(incident.getServiceId());
                PriorityResponse priority = independentServiceClient.validatePriority(incident.getPriorityId());

                // Determine who to notify based on assignment status
                Long reporterId = incident.getReportedBy();
                Long managerId = incident.getAssignedManagerId();
                Long supportEngineerId = incident.getAssignedTo();

                // Log assignment status for debugging
                logAssignmentStatus(incident.getIncidentId(), "Response Warning",
                        reporterId, managerId, supportEngineerId);

                // Publish warning event - nulls will be handled in the publisher
                incidentEventPublisher.publishSlaWarning(
                        incident,
                        "RESPONSE",
                        (int) minutesRemaining,
                        sla.getResponseDeadline(),
                        priority.getPriorityLevel(),
                        service.getServiceName(),
                        reporterId,
                        managerId,
                        supportEngineerId
                );

                // Mark warning as published
                sla.setResponseWarningPublished(true);
                incidentSlaRepository.save(sla);

                log.info("Published response warning for Incident ID: {}, {} minutes remaining",
                        incident.getIncidentId(), minutesRemaining);

            } catch (Exception e) {
                log.error("Failed to process response warning for SLA ID: {}",
                        sla.getIncidentSlaId(), e);
            }
        }
    }

    private void processResolutionWarnings(LocalDateTime now, LocalDateTime warningThreshold) {
        List<IncidentSla> approachingResolution =
                incidentSlaRepository.findApproachingResolutionDeadline(now, warningThreshold);

        log.info("Found {} incidents approaching resolution deadline", approachingResolution.size());

        for (IncidentSla sla : approachingResolution) {
            try {
                Incident incident = sla.getIncident();

                long minutesRemaining = ChronoUnit.MINUTES.between(now, sla.getResolutionDeadline());

                // Fetch service and priority details
                ServicesResponse service = independentServiceClient.validateService(incident.getServiceId());
                PriorityResponse priority = independentServiceClient.validatePriority(incident.getPriorityId());

                // Determine who to notify based on assignment status
                Long reporterId = incident.getReportedBy();
                Long managerId = incident.getAssignedManagerId();
                Long supportEngineerId = incident.getAssignedTo();

                // Log assignment status for debugging
                logAssignmentStatus(incident.getIncidentId(), "Resolution Warning",
                        reporterId, managerId, supportEngineerId);

                // Publish warning event - nulls will be handled in the publisher
                incidentEventPublisher.publishSlaWarning(
                        incident,
                        "RESOLUTION",
                        (int) minutesRemaining,
                        sla.getResolutionDeadline(),
                        priority.getPriorityLevel(),
                        service.getServiceName(),
                        reporterId,
                        managerId,
                        supportEngineerId
                );

                // Mark warning as published
                sla.setResolutionWarningPublished(true);
                incidentSlaRepository.save(sla);

                log.info("Published resolution warning for Incident ID: {}, {} minutes remaining",
                        incident.getIncidentId(), minutesRemaining);

            } catch (Exception e) {
                log.error("Failed to process resolution warning for SLA ID: {}",
                        sla.getIncidentSlaId(), e);
            }
        }
    }

    private void processResponseBreaches(LocalDateTime now) {
        List<IncidentSla> breaches = incidentSlaRepository.findResponseBreaches(now);

        log.info("Found {} response SLA breaches", breaches.size());

        for (IncidentSla sla : breaches) {
            try {
                Incident incident = sla.getIncident();

                long minutesOverdue = ChronoUnit.MINUTES.between(sla.getResponseDeadline(), now);

                // Fetch service and priority details
                ServicesResponse service = independentServiceClient.validateService(incident.getServiceId());
                PriorityResponse priority = independentServiceClient.validatePriority(incident.getPriorityId());

                // Determine who to notify based on assignment status
                Long reporterId = incident.getReportedBy();
                Long managerId = incident.getAssignedManagerId();
                Long supportEngineerId = incident.getAssignedTo();

                // Log assignment status for debugging
                logAssignmentStatus(incident.getIncidentId(), "Response Breach",
                        reporterId, managerId, supportEngineerId);

                // Publish breach event - nulls will be handled in the publisher
                incidentEventPublisher.publishSlaBreach(
                        incident,
                        "RESPONSE",
                        sla.getResponseDeadline(),
                        now,
                        (int) minutesOverdue,
                        priority.getPriorityLevel(),
                        service.getServiceName(),
                        reporterId,
                        managerId,
                        supportEngineerId
                );

                // Mark as breached
                sla.setResponseBreached(true);
                incidentSlaRepository.save(sla);

                log.warn("Published response breach for Incident ID: {}, {} minutes overdue",
                        incident.getIncidentId(), minutesOverdue);

            } catch (Exception e) {
                log.error("Failed to process response breach for SLA ID: {}",
                        sla.getIncidentSlaId(), e);
            }
        }
    }

    private void processResolutionBreaches(LocalDateTime now) {
        List<IncidentSla> breaches = incidentSlaRepository.findResolutionBreaches(now);

        log.info("Found {} resolution SLA breaches", breaches.size());

        for (IncidentSla sla : breaches) {
            try {
                Incident incident = sla.getIncident();

                long minutesOverdue = ChronoUnit.MINUTES.between(sla.getResolutionDeadline(), now);

                // Fetch service and priority details
                ServicesResponse service = independentServiceClient.validateService(incident.getServiceId());
                PriorityResponse priority = independentServiceClient.validatePriority(incident.getPriorityId());

                // Determine who to notify based on assignment status
                Long reporterId = incident.getReportedBy();
                Long managerId = incident.getAssignedManagerId();
                Long supportEngineerId = incident.getAssignedTo();

                // Log assignment status for debugging
                logAssignmentStatus(incident.getIncidentId(), "Resolution Breach",
                        reporterId, managerId, supportEngineerId);

                // Publish breach event - nulls will be handled in the publisher
                incidentEventPublisher.publishSlaBreach(
                        incident,
                        "RESOLUTION",
                        sla.getResolutionDeadline(),
                        now,
                        (int) minutesOverdue,
                        priority.getPriorityLevel(),
                        service.getServiceName(),
                        reporterId,
                        managerId,
                        supportEngineerId
                );

                // Mark as breached
                sla.setResolutionBreached(true);
                incidentSlaRepository.save(sla);

                log.warn("Published resolution breach for Incident ID: {}, {} minutes overdue",
                        incident.getIncidentId(), minutesOverdue);

            } catch (Exception e) {
                log.error("Failed to process resolution breach for SLA ID: {}",
                        sla.getIncidentSlaId(), e);
            }
        }
    }

    /**
     * Helper method to log assignment status for debugging
     */
    private void logAssignmentStatus(Long incidentId, String eventType,
                                     Long reporterId, Long managerId, Long supportEngineerId) {
        StringBuilder status = new StringBuilder();
        status.append(String.format("Incident ID: %d | %s | ", incidentId, eventType));
        status.append(String.format("Reporter: %s | ", reporterId != null ? reporterId : "ASSIGNED"));
        status.append(String.format("Manager: %s | ", managerId != null ? managerId : "NOT_ASSIGNED"));
        status.append(String.format("Support Engineer: %s",
                supportEngineerId != null ? supportEngineerId : "NOT_ASSIGNED"));

        log.debug(status.toString());
    }

    public List<IncidentSlaResponse> getIncidentSlas(SlaStatus slaStatus) {

        log.info("Attempting to fetch {} Incident_SLA", slaStatus);

        List<IncidentSla> resp;

        switch (slaStatus) {
            case ACTIVE -> resp = incidentSlaRepository.findAllByIsActive(true);
            case CLOSED -> resp = incidentSlaRepository.findAllByIsActive(false);
            case ALL -> resp = incidentSlaRepository.findAll();
            default -> throw new IllegalArgumentException("Invalid SLA status");
        }

        return IncidentSlaMapper.toResponseList(resp);
    }

}