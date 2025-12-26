// SlaRulesMapper.java - Removed unused method
package com.trackIt.sla_service.mapper;

import com.trackIt.sla_service.dto.SlaRulesPriorityResponse;
import com.trackIt.sla_service.dto.SlaRulesRequest;
import com.trackIt.sla_service.dto.SlaRulesResponse;
import com.trackIt.sla_service.model.SlaRules;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SlaRulesMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static List<SlaRulesResponse<String>> toResponseList(
            List<SlaRules> rules,
            Map<Long, String> priorityMap
    ) {
        if (rules == null || rules.isEmpty()) {
            return List.of();
        }

        return rules.stream()
                .map(rule -> toResponseWithPriorityName(
                        rule,
                        priorityMap.get(rule.getPriorityId())
                ))
                .toList();
    }

    public static SlaRulesResponse<String> toResponseWithPriorityName(
            SlaRules rules,
            String priorityName
    ) {
        if (rules == null) {
            return null;
        }

        return SlaRulesResponse.<String>builder()
                .serviceId(rules.getServiceId())
                .priorityLevel(priorityName)
                .response_time_mins(rules.getResponse_time_mins())
                .resolution_time_mins(rules.getResolution_time_mins())
                .createdAt(
                        rules.getCreatedAt() != null
                                ? rules.getCreatedAt().format(FORMATTER)
                                : null
                )
                .updatedAt(
                        rules.getUpdatedAt() != null
                                ? rules.getUpdatedAt().format(FORMATTER)
                                : null
                )
                .isActive(rules.isActive())
                .build();
    }

    public static SlaRulesResponse<Long> toResponseWithPriorityId(
            SlaRules rules
    ) {
        if (rules == null) {
            return null;
        }

        return SlaRulesResponse.<Long>builder()
                .serviceId(rules.getServiceId())
                .priorityLevel(rules.getPriorityId())
                .response_time_mins(rules.getResponse_time_mins())
                .resolution_time_mins(rules.getResolution_time_mins())
                .createdAt(
                        rules.getCreatedAt() != null
                                ? rules.getCreatedAt().format(FORMATTER)
                                : null
                )
                .updatedAt(
                        rules.getUpdatedAt() != null
                                ? rules.getUpdatedAt().format(FORMATTER)
                                : null
                )
                .isActive(rules.isActive())
                .build();
    }

    public static SlaRules toSlaRules(SlaRulesRequest request) {
        if(request == null){
            return null;
        }

        return SlaRules.builder()
                .serviceId(request.getServiceId())
                .priorityId(request.getPriorityId())
                .response_time_mins(request.getResponse_time_mins())
                .resolution_time_mins(request.getResolution_time_mins())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();
    }

    public static List<SlaRulesPriorityResponse> toSlaPriorityResponseList(List<SlaRules> list) {
        if (list == null || list.isEmpty()){
            return List.of();
        }
        return list.stream()
                .map(SlaRulesMapper::toSlaPriorityResponse)
                .collect(Collectors.toList());
    }

    public static SlaRulesPriorityResponse toSlaPriorityResponse(SlaRules list) {
        if(list == null){
            return null;
        }

        return SlaRulesPriorityResponse.builder()
                .slaRulesId(list.getSlaId())
                .serviceId(list.getServiceId())
                .response_time_mins(list.getResponse_time_mins())
                .resolution_time_mins(list.getResolution_time_mins())
                .build();
    }
}