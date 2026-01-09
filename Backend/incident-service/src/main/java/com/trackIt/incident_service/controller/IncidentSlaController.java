package com.trackIt.incident_service.controller;

import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.response.IncidentSlaResponse;
import com.trackIt.incident_service.model.SlaStatus;
import com.trackIt.incident_service.service.IncidentSlaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/incidentSla")
public class IncidentSlaController {

    private final IncidentSlaService incidentSlaService;

    @GetMapping("/{status}")
    public ResponseEntity<ApiResponse<List<IncidentSlaResponse>>> getList(
            @PathVariable String status) {

        SlaStatus slaStatus = SlaStatus.from(status);

        log.info("REST received to fetch all the {} Incident_sla.", slaStatus);

        List<IncidentSlaResponse> list =
                incidentSlaService.getIncidentSlas(slaStatus);

        return ResponseEntity.ok(ApiResponse.success(
                String.format("Fetched list of size %d", list.size()),
                list
        ));
    }


}
