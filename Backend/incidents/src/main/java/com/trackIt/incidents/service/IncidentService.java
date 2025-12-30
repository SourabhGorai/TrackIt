package com.trackIt.incidents.service;

import com.trackIt.incidents.client.IndependentServiceClient;
import com.trackIt.incidents.client.UserServiceClient;
import com.trackIt.incidents.dto.request.AssignSupportEngineerRequest;
import com.trackIt.incidents.dto.request.ReporterRequest;
import com.trackIt.incidents.dto.request.SupporterRequest;
import com.trackIt.incidents.dto.response.*;
import com.trackIt.incidents.exception.NotFoundException;
import com.trackIt.incidents.exception.ServiceException;
import com.trackIt.incidents.exception.UserNotFoundException;
import com.trackIt.incidents.mapper.IncidentMapper;
import com.trackIt.incidents.model.Incident;
import com.trackIt.incidents.model.Status;
import com.trackIt.incidents.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IndependentServiceClient independentServiceClient;
    private final UserServiceClient userServiceClient;


    @Transactional
    public IncidentResponse createIncident(Long userId, ReporterRequest incident) {

        log.info("Create incident request received");

        incident.setTitle(IncidentMapper.sanitizeTitle(incident.getTitle()));
        incident.setDescription(IncidentMapper.sanitizeDescription(incident.getDescription()));

        LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);

        Optional<Incident> existingOpt =
                incidentRepository.findFirstByTitleAndServiceIdAndReportedByAndStatusAndCreatedAtAfter(
                        incident.getTitle(),
                        incident.getServiceId(),
                        userId,
                        Status.OPEN,
                        thirtyMinutesAgo
                );

        UserResponsePublic user = userServiceClient.getUserDetails(userId);
        String reporterName = user.getName();

        String priority = independentServiceClient
                .validatePriority(incident.getPriorityId())
                .getPriorityLevel();

        if (existingOpt.isPresent()) {
            Incident existing = existingOpt.get();

            log.info("Duplicate incident found with ID: {}", existing.getIncidentId());

            String assignedName = existing.getAssignedTo() != null
                    ? userServiceClient.getUserDetails(existing.getAssignedTo()).getName()
                    : null;

            return IncidentMapper.toResponse(existing, reporterName, priority, assignedName);
        }

        try {
            Incident saved = incidentRepository.save(
                    IncidentMapper.reporterRequestToIncident(incident, userId)
            );

            log.info("Incident saved successfully with ID: {}", saved.getIncidentId());

            return IncidentMapper.toResponse(saved, reporterName, priority, null);

        } catch (Exception e) {
            log.error("Failed to save incident", e);
            throw new ServiceException("Failed to save incident data", e);
        }
    }


    @Transactional
    public String assignProviderManager(Long incidentId, Long userId) {

        log.info("Attempting to assign Provider Manager. IncidentId={}, UserId={}", incidentId, userId);

        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ServiceException("Incident not found"));

        if (incident.getAssignedManagerId() != null) {
            log.warn("Incident {} already has a Provider Manager assigned", incidentId);
            throw new ServiceException("Provider Manager is already assigned to this incident");
        }

        // Assign PM
        incident.setAssignedManagerId(userId);
        incidentRepository.save(incident);

        // Fetch PM details (Feign / REST call)
        UserResponsePublic user = userServiceClient.getUserDetails(userId);

        return String.format("%s (%s)", user.getName(), user.getEmployeeId());
    }


    @Transactional
    public IncidentResponse assignSupportEngineer(AssignSupportEngineerRequest req) {

        log.info("Assign support engineer request received for ID: {}", req.getIncidentId());

        Incident incident = incidentRepository.findById(req.getIncidentId())
                .orElseThrow(() ->
                        new NotFoundException("Incident", req.getIncidentId().toString())
                );

        UserResponsePublic reporter = Optional
                .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                .orElseThrow(() -> new ServiceException("Reporter not found"));

        PriorityResponse priority = Optional
                .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
                .orElseThrow(() -> new ServiceException("Invalid priority"));

        if (incident.getAssignedTo() != null) {

            UserResponsePublic assigned = Optional
                    .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
                    .orElseThrow(() -> new ServiceException("Assigned user not found"));

            log.info("Already assigned | EmpID: {} | Name: {} | Status: {}",
                    assigned.getEmployeeId(),
                    assigned.getName(),
                    incident.getStatus());

            return IncidentMapper.toResponse(
                    incident,
                    reporter.getName(),
                    priority.getPriorityLevel(),
                    assigned.getName()
            );
        }

        UserResponsePublic assigned = Optional
                .ofNullable(userServiceClient.getUserDetailsByEmployeeId(req.getAssignedTo()))
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with employee ID: " + req.getAssignedTo()
                        )
                );

        RoleResponse role = Optional
                .ofNullable(independentServiceClient.validateRole(assigned.getRoleId()))
                .orElseThrow(() -> new ServiceException("Unable to validate role"));

        if (!"SUPPORT_ENGINEER".equals(role.getRole())) {
            throw new ServiceException("User is not a support engineer");
        }

        incident.setAssignedTo(assigned.getId());
        incident.setStatus(Status.IN_PROGRESS);

        Incident saved = incidentRepository.save(incident);

        return IncidentMapper.toResponse(
                saved,
                reporter.getName(),
                priority.getPriorityLevel(),
                assigned.getName()
        );
    }


    @Transactional
    public IncidentResponse changeStatus(SupporterRequest req) {

        Long id = req.getIncidentId();
        Status status = req.getStatus();

        log.info("Request received to change status of incident ID: {}", id);

        Optional<Incident> incidentOpt = incidentRepository.findById(id);

        if (incidentOpt.isEmpty()) {
            throw new ServiceException(String.format(
                    "Incident record does not exists with ID: %s", id.toString()));
        }

        try {
            Incident incident = incidentOpt.get();

            incident.setStatus(status);
            Incident saved = incidentRepository.save(incident);

            UserResponsePublic reporter = Optional
                    .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                    .orElseThrow(() -> new ServiceException("Reporter not found"));

            PriorityResponse priority = Optional
                    .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
                    .orElseThrow(() -> new ServiceException("Invalid priority"));

            UserResponsePublic assigned = Optional
                    .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
                    .orElseThrow(() -> new ServiceException("Assigned user not found"));

            return IncidentMapper.toResponse(
                    saved,
                    reporter.getName(),
                    priority.getPriorityLevel(),
                    assigned.getName()
            );

        } catch (Exception e) {
            log.info("Failed to Update Status of incident with ID: {}", id);
            throw new ServiceException("Failed to Update Status", e);
        }
    }


    public List<IncidentResponse> getAll() {

        log.info("Attempting to fetch all incidents");

        List<Incident> incidents = incidentRepository.findAll();

        if (incidents.isEmpty()) {
            log.info("No incidents found");
            return List.of();
        }

        return IncidentMapper.toResponseList(
                incidents,
                userServiceClient,
                independentServiceClient
        );
    }

    public IncidentResponse getById(Long id) {

        log.info("Attempting to fetch incident with ID: {}", id);

        Incident incident = incidentRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Incident", id.toString())
        );

        UserResponsePublic reporter = Optional
                .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                .orElseThrow(() -> new ServiceException("Reporter not found."));

        UserResponsePublic assigned = Optional
                .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
                .orElseThrow(() -> new ServiceException("Assigned person not found."));

        PriorityResponse priority = Optional
                .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
                .orElseThrow(() -> new ServiceException("Priority not found"));

        return IncidentMapper.toResponse(
                incident,
                reporter.getName(),
                priority.getPriorityLevel(),
                assigned.getName());
    }

}
