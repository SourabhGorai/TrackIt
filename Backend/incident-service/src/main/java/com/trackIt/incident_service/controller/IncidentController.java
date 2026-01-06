package com.trackIt.incident_service.controller;

import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.request.AssignSupportEngineerRequest;
import com.trackIt.incident_service.dto.request.ReporterRequest;
import com.trackIt.incident_service.dto.request.SupporterRequest;
import com.trackIt.incident_service.dto.response.IncidentResponse;
import com.trackIt.incident_service.dto.response.PreciseResponse;
import com.trackIt.incident_service.service.IncidentService;
import com.trackIt.incident_service.service.JwtService;
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
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident (
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ReporterRequest request) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);

        log.info("REST received to create an incident for Service ID: {}, with criticality: {}",
                request.getServiceId(), request.getPriorityId());

        IncidentResponse response = incidentService.createIncident(userId, request);

        return ResponseEntity.ok(ApiResponse.success(
                "Created Successfully", response
        ));
    }

    @PostMapping("/providerManager/{incidentId}")
    public ResponseEntity<ApiResponse<?>> pmAllocation(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long incidentId
    ) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);

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
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AssignSupportEngineerRequest req
            ) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
//        Long userId = jwtService.extractUserId(token);
//        String username = jwtService.extractUsername(token);
//        String role = jwtService.extractRole(token);


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
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SupporterRequest req) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

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

    @GetMapping("/getMyIncidents")
    public ResponseEntity<ApiResponse<List<PreciseResponse>>> getMyIncidents(
            @RequestHeader("Authorization") String authHeader
    ) {

        // Extract JWT token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract user details from token
        Long userId = jwtService.extractUserId(token);
        String username = jwtService.extractUsername(token);
        String role = jwtService.extractRole(token);

        log.info("REST received to fetch incidents of {}", username);

        List<PreciseResponse> resp = incidentService.getMyIncidents(userId, role);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Successfully fetched all the incidents associated with %s: %s",
                        role, username),
                resp
        ));

    }

    //******************* USED IN USERS-SERVICE ********************//

    @GetMapping("/supportEngineer/isAvailable")
    public ResponseEntity<ApiResponse<List<Long>>> getBusySupportEngineer(
            @RequestBody List<Long> ids
    ) {

        log.info("REST received to check availability status of ids: {}", ids);

        List<Long> resp = incidentService.checkBusySE(ids);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list of size: %d", resp.size()),
                resp
        ));

    }

}
