package com.trackIt.incident_service.service;

import com.trackIt.incident_service.client.IndependentServiceClient;
import com.trackIt.incident_service.client.SlaServiceClient;
import com.trackIt.incident_service.client.UserServiceClient;
import com.trackIt.incident_service.dto.request.AssignSupportEngineerRequest;
import com.trackIt.incident_service.dto.request.ReporterRequest;
import com.trackIt.incident_service.dto.request.SupporterRequest;
import com.trackIt.incident_service.dto.response.*;
import com.trackIt.incident_service.exception.NotFoundException;
import com.trackIt.incident_service.exception.ServiceException;
import com.trackIt.incident_service.exception.UserNotFoundException;
import com.trackIt.incident_service.mapper.IncidentMapper;
import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.model.Status;
import com.trackIt.incident_service.publisher.IncidentEventPublisher;
import com.trackIt.incident_service.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IndependentServiceClient independentServiceClient;
    private final UserServiceClient userServiceClient;
    private final SlaServiceClient slaServiceClient;
    private final IncidentEventPublisher incidentEventPublisher;

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
        log.info("User info: {}", user);
        String reporterName = user.getName();

        String priority = independentServiceClient
                .validatePriority(incident.getPriorityId())
                .getPriorityLevel();

        // ---- DUPLICATE INCIDENT (NO EVENT PUBLISHED BY DESIGN) ----
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

            ServicesResponse service =
                    independentServiceClient.validateService(saved.getServiceId());

//            ********** Already getting it from the users **********
//            CompanyResponse clientCompany =
//                    independentServiceClient.validateCompany(service.getClientCompanyId());

            CompanyResponse providerCompany =
                    independentServiceClient.validateCompany(service.getProviderCompanyId());

            // 🔐 Publish ONLY after successful DB commit
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            incidentEventPublisher.publishIncidentCreated(
                                    saved,
                                    priority,
                                    providerCompany.getCompanyId(),
                                    user.getCompanyName(),
                                    service.getServiceId(),
                                    service.getServiceName()
                            );
                        }
                    }
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

        UserResponsePublic user = userServiceClient.getUserDetails(userId);
        Long serviceId = incidentRepository.findById(incidentId).orElseThrow().getServiceId();

        ServicesResponse service = independentServiceClient.validateService(serviceId);

        if (service.getProviderCompanyId() != user.getCompanyId()) {
            throw new ServiceException("You are not an employee of the provider company.");
        }

        if (incident.getAssignedManagerId() != null) {
            log.warn("Incident {} already has a Provider Manager assigned", incidentId);
            throw new ServiceException("Provider Manager is already assigned to this incident");
        }

        // Assign PM
        incident.setAssignedManagerId(userId);
        incidentRepository.save(incident);

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

        ServicesResponse service = Optional
                .ofNullable(independentServiceClient.validateService(incident.getServiceId()))
                .orElseThrow(() ->
                        new ServiceException(String.format("Service not found Id: %s",
                                incident.getServiceId().toString()))
                );

        if (!service.getProviderCompanyId().equals(assigned.getCompanyId())) {
            throw new ServiceException(String.format(
                    "Employee Id: %s, is not of the provider company.",
                    req.getAssignedTo()
            ));
        }

        RoleResponse role = Optional
                .ofNullable(independentServiceClient.validateRole(assigned.getRoleId()))
                .orElseThrow(() -> new ServiceException("Unable to validate role"));

        if (!"SUPPORT_ENGINEER".equals(role.getRole())) {
            throw new ServiceException("User is not a support engineer");
        }

        Status previousStatus = incident.getStatus();
        incident.setAssignedTo(assigned.getId());
        incident.setStatus(Status.IN_PROGRESS);

        Incident saved = incidentRepository.save(incident);

        // 🔐 Publish event ONLY after successful DB commit
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        incidentEventPublisher.publishSupportEngineerAssigned(
                                saved,
                                previousStatus,
                                priority.getPriorityLevel(),
                                service.getServiceName(),
                                reporter.getId(),
                                incident.getAssignedManagerId(),
                                assigned.getId(),
                                assigned.getName(),
                                assigned.getEmployeeId()
                        );
                    }
                }
        );

        log.info("Successfully assigned support engineer {} to incident {}",
                assigned.getName(), saved.getIncidentId());

        return IncidentMapper.toResponse(
                saved,
                reporter.getName(),
                priority.getPriorityLevel(),
                assigned.getName()
        );
    }

//    @Transactional
//    public IncidentResponse assignSupportEngineer(AssignSupportEngineerRequest req) {
//
//        log.info("Assign support engineer request received for ID: {}", req.getIncidentId());
//
//        Incident incident = incidentRepository.findById(req.getIncidentId())
//                .orElseThrow(() ->
//                        new NotFoundException("Incident", req.getIncidentId().toString())
//                );
//
//        UserResponsePublic reporter = Optional
//                .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
//                .orElseThrow(() -> new ServiceException("Reporter not found"));
//
//        PriorityResponse priority = Optional
//                .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
//                .orElseThrow(() -> new ServiceException("Invalid priority"));
//
//        if (incident.getAssignedTo() != null) {
//
//            UserResponsePublic assigned = Optional
//                    .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
//                    .orElseThrow(() -> new ServiceException("Assigned user not found"));
//
//            log.info("Already assigned | EmpID: {} | Name: {} | Status: {}",
//                    assigned.getEmployeeId(),
//                    assigned.getName(),
//                    incident.getStatus());
//
//            return IncidentMapper.toResponse(
//                    incident,
//                    reporter.getName(),
//                    priority.getPriorityLevel(),
//                    assigned.getName()
//            );
//        }
//
//        UserResponsePublic assigned = Optional
//                .ofNullable(userServiceClient.getUserDetailsByEmployeeId(req.getAssignedTo()))
//                .orElseThrow(() ->
//                        new UserNotFoundException(
//                                "User not found with employee ID: " + req.getAssignedTo()
//                        )
//                );
//
//        ServicesResponse service = Optional
//                .ofNullable(independentServiceClient.validateService(incident.getServiceId()))
//                .orElseThrow(() ->
//                        new ServiceException(String.format("Service not found Id: %s",
//                                incident.getServiceId().toString()))
//                );
//
//        if (!service.getProviderCompanyId().equals(assigned.getCompanyId())) {
//            throw new ServiceException(String.format(
//                    "Employee Id: %s, is not of the provider company.",
//                    req.getAssignedTo()
//            ));
//        }
//
//        RoleResponse role = Optional
//                .ofNullable(independentServiceClient.validateRole(assigned.getRoleId()))
//                .orElseThrow(() -> new ServiceException("Unable to validate role"));
//
//        if (!"SUPPORT_ENGINEER".equals(role.getRole())) {
//            throw new ServiceException("User is not a support engineer");
//        }
//
//        incident.setAssignedTo(assigned.getId());
//        incident.setStatus(Status.IN_PROGRESS);
//
//        Incident saved = incidentRepository.save(incident);
//
//        return IncidentMapper.toResponse(
//                saved,
//                reporter.getName(),
//                priority.getPriorityLevel(),
//                assigned.getName()
//        );
//    }


//    @Transactional
//    public IncidentResponse changeStatus(SupporterRequest req) {
//
//        Long id = req.getIncidentId();
//        Status status = req.getStatus();
//
//        log.info("Request received to change status of incident ID: {}", id);
//
//        Optional<Incident> incidentOpt = incidentRepository.findById(id);
//
//        if (incidentOpt.isEmpty()) {
//            throw new ServiceException(String.format(
//                    "Incident record does not exists with ID: %s", id.toString()));
//        }
//
//        try {
//            Incident incident = incidentOpt.get();
//
//            UserResponsePublic reporter = Optional
//                    .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
//                    .orElseThrow(() -> new ServiceException("Reporter not found"));
//
//            PriorityResponse priority = Optional
//                    .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
//                    .orElseThrow(() -> new ServiceException("Invalid priority"));
//
//            UserResponsePublic assigned = Optional
//                    .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
//                    .orElseThrow(() -> new ServiceException("Assigned user not found"));
//
//            if (incident.getStatus() != null && incident.getStatus() == status) {
//                log.info("Status already set to: {}", status);
//                return IncidentMapper.toResponse(
//                        incident,
//                        reporter.getName(),
//                        priority.getPriorityLevel(),
//                        assigned.getName()
//                );
//            }
//
//            incident.setStatus(status);
//
//            Incident saved = incidentRepository.save(incident);
//
//            if (status == Status.RESOLVED) {
//                incident.setResolvedAt(LocalDateTime.now());
//                // Kafka will send notification to reporter, provider_manager and support engineer
//                // that the issue is resolved.
//            } else if (status == Status.OPEN) {
//                // send notification to the reporter
//            } else if (status == Status.IN_PROGRESS || status == Status.ON_HOLD || status == Status.CLOSED) {
//                //send notification to reporter, provider_manager, assignedTo
//            }
//
//            return IncidentMapper.toResponse(
//                    saved,
//                    reporter.getName(),
//                    priority.getPriorityLevel(),
//                    assigned.getName()
//            );
//
//        } catch (Exception e) {
//            log.info("Failed to Update Status of incident with ID: {}", id);
//            throw new ServiceException("Failed to Update Status", e);
//        }
//    }

    @Transactional
    public IncidentResponse changeStatus(SupporterRequest req, Long userId, String role) {

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

            boolean isAdmin = "ADMIN".equals(role);

            boolean isAssignedManager =
                    userId.equals(incident.getAssignedManagerId());

            boolean isAssignedSupport =
                    userId.equals(incident.getAssignedTo());

            boolean isReporter =
                    userId.equals(incident.getReportedBy());

            if (!(isAdmin || isAssignedManager || isAssignedSupport || isReporter)) {
                throw new ServiceException(
                        String.format(
                                "You are not allowed to change the status of the incident: %s",
                                incident.getIncidentId()
                        )
                );
            }

            UserResponsePublic reporter = Optional
                        .ofNullable(userServiceClient.getUserDetails(incident.getReportedBy()))
                        .orElseThrow(() -> new ServiceException("Reporter not found"));

            PriorityResponse priority = Optional
                    .ofNullable(independentServiceClient.validatePriority(incident.getPriorityId()))
                    .orElseThrow(() -> new ServiceException("Invalid priority"));

            UserResponsePublic assigned = Optional
                    .ofNullable(userServiceClient.getUserDetails(incident.getAssignedTo()))
                    .orElseThrow(() -> new ServiceException("Assigned user not found"));

            if (incident.getStatus() != null && incident.getStatus() == status) {
                log.info("Status already set to: {}", status);
                return IncidentMapper.toResponse(
                        incident,
                        reporter.getName(),
                        priority.getPriorityLevel(),
                        assigned.getName()
                );
            }

            Status previousStatus = incident.getStatus();
            incident.setStatus(status);

            if (status == Status.RESOLVED) {
                incident.setResolvedAt(LocalDateTime.now());
            }

            Incident saved = incidentRepository.save(incident);

            // Get service details for event
            ServicesResponse service =
                    independentServiceClient.validateService(saved.getServiceId());

            // 🔐 Publish events ONLY after successful DB commit
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            publishStatusChangeEvent(
                                    saved,
                                    status,
                                    previousStatus,
                                    reporter,
                                    assigned,
                                    priority.getPriorityLevel(),
                                    service
                            );
                        }
                    }
            );

            log.info("Successfully updated status of incident {} from {} to {}",
                    id, previousStatus, status);

            return IncidentMapper.toResponse(
                    saved,
                    reporter.getName(),
                    priority.getPriorityLevel(),
                    assigned.getName()
            );

        } catch (Exception e) {
            log.error("Failed to Update Status of incident with ID: {}", id, e);
            throw new ServiceException("Failed to Update Status", e);
        }
    }

    private void publishStatusChangeEvent(
            Incident incident,
            Status newStatus,
            Status previousStatus,
            UserResponsePublic reporter,
            UserResponsePublic assigned,
            String priority,
            ServicesResponse service
    ) {
        switch (newStatus) {
            case RESOLVED -> {
                // Notify reporter, provider_manager and support engineer
                incidentEventPublisher.publishIncidentStatusChanged(
                        incident,
                        newStatus,
                        previousStatus,
                        priority,
                        service.getServiceName(),
                        reporter.getId(),
                        incident.getAssignedManagerId(),
                        assigned.getId()
                );
                log.info("Published RESOLVED event for incident {}", incident.getIncidentId());
            }
            case OPEN -> {
                // Notify the reporter
                incidentEventPublisher.publishIncidentStatusChanged(
                        incident,
                        newStatus,
                        previousStatus,
                        priority,
                        service.getServiceName(),
                        reporter.getId(),
                        null,
                        null
                );
                log.info("Published OPEN event for incident {}", incident.getIncidentId());
            }
            case IN_PROGRESS, ON_HOLD, CLOSED -> {
                // Notify reporter, provider_manager, assignedTo
                incidentEventPublisher.publishIncidentStatusChanged(
                        incident,
                        newStatus,
                        previousStatus,
                        priority,
                        service.getServiceName(),
                        reporter.getId(),
                        incident.getAssignedManagerId(),
                        assigned.getId()
                );
                log.info("Published {} event for incident {}", newStatus, incident.getIncidentId());
            }
            default -> log.warn("Unknown status: {} for incident {}", newStatus, incident.getIncidentId());
        }
    }

    @Transactional
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

    @Transactional
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

    @Transactional
    public List<PreciseResponse> getIncidentsByCompanyId(Long companyId) {

        log.info("Fetching incidents for company ID: {}", companyId);

        CompanyResponse company =
                independentServiceClient.validateCompany(companyId);

        if (company == null) {
            throw new NotFoundException("Company", companyId.toString());
        }

        List<Long> serviceIds =
                independentServiceClient.getServiceIdList(companyId);

        if (serviceIds == null || serviceIds.isEmpty()) {
            log.info("No services found for company ID: {}", companyId);
            return List.of();
        }

        List<Incident> incidents =
                incidentRepository.findByServiceIdIn(serviceIds);

        if (incidents.isEmpty()) {
            log.info("No incidents found for company ID: {}", companyId);
            return List.of();
        }

        return IncidentMapper.toPreciseResponseList(
                incidents,
                userServiceClient,
                independentServiceClient,
                slaServiceClient
        );
    }

    @Transactional(readOnly = true)
    public List<Long> checkBusySE(List<Long> ids) {

        log.info("Attempting to get busy support engineers");

        if (ids == null || ids.isEmpty()) {
            return List.of(); // always return a list
        }

        // IDs that are already assigned to incidents
        List<Long> busyIds = incidentRepository.findAssignedToIds(ids);

        log.info("Here are the busy ids: {}", busyIds);

        // If none are busy, return empty list
        return busyIds == null ? List.of() : busyIds;
    }


    @Transactional
    public List<PreciseResponse> getMyIncidents(Long userId, String role) {

        log.info("Attempting to fetch all the incidents associated with {}: {}", role, userId);

        if (userId == null || role == null) {
            throw new ServiceException("UserId or role cannot be null");
        }

        List<Incident> incidents;

        switch (role) {
            case "PROVIDER_MANAGER" -> incidents = incidentRepository.findAllByAssignedManagerId(userId);

            case "SUPPORT_ENGINEER" -> incidents = incidentRepository.findAllByAssignedTo(userId);

            case "REPORTER" -> incidents = incidentRepository.findAllByReportedBy(userId);

            default -> throw new ServiceException("Invalid role: " + role);
        }

        if (incidents.isEmpty()) {
            log.info("No incidents found for userId={} with role={}", userId, role);
            return List.of();
        }

        return IncidentMapper.toPreciseResponseList(
                incidents,
                userServiceClient,
                independentServiceClient,
                slaServiceClient
        );
    }

    public List<String> getAllStatusTypes() {

        log.info("Attempting to fetch all the status types");

        return Arrays.stream(Status.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}
