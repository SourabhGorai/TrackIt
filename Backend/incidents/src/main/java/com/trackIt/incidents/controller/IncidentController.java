package com.trackIt.incidents.controller;

import com.trackIt.incidents.dto.ApiResponse;
import com.trackIt.incidents.dto.request.AssignSupportEngineerRequest;
import com.trackIt.incidents.dto.request.ReporterRequest;
import com.trackIt.incidents.dto.request.SupporterRequest;
import com.trackIt.incidents.dto.response.IncidentResponse;
import com.trackIt.incidents.dto.response.PreciseResponse;
import com.trackIt.incidents.service.IncidentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident (
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ReporterRequest request) {

        log.info("REST received to create an incident for Service ID: {}, with criticality: {}",
                request.getServiceId(), request.getPriorityId());

        IncidentResponse response = incidentService.createIncident(userId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Created Successfully", response
        ));
    }

    @PostMapping("/providerManager/{incidentId}")
    public ResponseEntity<ApiResponse<?>> pmAllocation(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long incidentId
    ) {

        log.info("REST request to assign Provider Manager for Incident ID: {}", incidentId);

        String assignedPm = incidentService.assignProviderManager(incidentId, userId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format(
                                "Incident %d successfully assigned to Provider Manager: %s",
                                incidentId, assignedPm
                        )
                )
        );
    }

    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<IncidentResponse>> assignSupportEngineer (
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody AssignSupportEngineerRequest req
            ) {

        log.info("REST received to assign an support engineer for incident with ID: {}",
                req.getIncidentId());

        IncidentResponse response = incidentService.assignSupportEngineer(req);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Support Engineer Assigned to incident with ID: %d successfully",
                        response.getIncidentId()),
                        response
                )
        );

    }

    @PostMapping("/statusUpdate")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus (
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody SupporterRequest req) {

        log.info("REST received to change status of incident with ID: {}", req.getIncidentId());

        IncidentResponse response = incidentService.changeStatus(req);

        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("Successfully changed status of incident record with ID: %s",
                                req.getIncidentId().toString()),
                        response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getAll() {

        log.info("REST received to get all incidents");

        List<IncidentResponse> list = incidentService.getAll();

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched %d incidents", list.size()),
                list
        ));

    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<ApiResponse<IncidentResponse>> getById(@PathVariable Long id) {

        log.info("REST received to get incident by ID: {}", id);

        IncidentResponse incident = incidentService.getById(id);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully fetched incident with ID: %s", id.toString()),
                incident
        ));

    }

    @GetMapping("/preciseResponse/{compId}")
    public ResponseEntity<ApiResponse<List<PreciseResponse>>> getPreciseResponseByCompanyId(
            @PathVariable Long compId
    ) {

        log.info("REST request received to fetch precise details of incidents for company ID: {}",
                compId);

        List<PreciseResponse> resp = incidentService.getIncidentsByCompanyId(compId);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list of size: %d", resp.size()),
                resp
        ));
    }


}
