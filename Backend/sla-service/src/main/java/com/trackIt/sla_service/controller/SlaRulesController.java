package com.trackIt.sla_service.controller;

import com.trackIt.sla_service.dto.ApiResponse;
import com.trackIt.sla_service.dto.SlaRulesPriorityResponse;
import com.trackIt.sla_service.dto.SlaRulesRequest;
import com.trackIt.sla_service.dto.SlaRulesResponse;
import com.trackIt.sla_service.services.SlaRulesServices;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
@RequiredArgsConstructor
@Slf4j
public class SlaRulesController {

    private final SlaRulesServices slaRulesServices;

    @PostMapping
    public ResponseEntity<ApiResponse<SlaRulesResponse<Long>>> addRule(
            @RequestBody @Valid SlaRulesRequest request) {

        log.info("REST received to add SLA Rule with service ID: {}", request.getServiceId());

        SlaRulesResponse<Long> resp = slaRulesServices.addRule(request);

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Added SLA Rule with Service ID: %d",
                        resp.getServiceId()), resp)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaRulesResponse<String>>>> getAll() {

        log.info("REST received to list all SLA rules");

        List<SlaRulesResponse<String>> responses = slaRulesServices.getAll();

        return ResponseEntity.ok(
                ApiResponse.success(String.format("Received list of size %d",
                        responses.size()), responses)
        );
    }

    @GetMapping("/rule/{slaRuleId}")
    public ResponseEntity<ApiResponse<SlaRulesResponse<String>>> getById(
            @PathVariable Long slaRuleId) {

        log.info("REST received to fetch SLA rule by ID: {}", slaRuleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "SLA rule fetched successfully",
                        slaRulesServices.getBySlaRuleId(slaRuleId)
                )
        );
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<ApiResponse<List<SlaRulesResponse<String>>>> getByServiceId(
            @PathVariable Long serviceId) {

        log.info("REST received to fetch SLA rules by service ID: {}", serviceId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "SLA rules fetched successfully",
                        slaRulesServices.getByServiceId(serviceId)
                )
        );
    }

    @PutMapping("/rule/{slaRuleId}")
    public ResponseEntity<ApiResponse<SlaRulesResponse<Long>>> updateRule(
            @PathVariable Long slaRuleId,
            @RequestBody @Valid SlaRulesRequest request
    ) {
        log.info("REST received to update SLA rule ID: {}", slaRuleId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "SLA rule updated successfully",
                        slaRulesServices.updateRule(slaRuleId, request)
                )
        );
    }

    @DeleteMapping("/rule/{slaRuleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @PathVariable Long slaRuleId
    ) {
        log.info("REST received to delete SLA rule ID: {}", slaRuleId);

        slaRulesServices.deleteRule(slaRuleId);
        return ResponseEntity.ok(
                ApiResponse.success("SLA rule deleted successfully", null)
        );
    }

    @GetMapping("/priority/{priorityId}")
    public ResponseEntity<ApiResponse<List<SlaRulesPriorityResponse>>> getByPriority(
            @PathVariable Long priorityId
    ) {

        log.info("REST received to display service details for Priority ID: {}", priorityId);

        List<SlaRulesPriorityResponse> list = slaRulesServices.getByPriority(priorityId);
        return ResponseEntity.ok(
                ApiResponse.success(String.format(
                                "Fetched list of size: %d", list.size()),
                        list
                )
        );
    }
}