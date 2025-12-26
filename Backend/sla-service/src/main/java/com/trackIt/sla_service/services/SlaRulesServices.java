package com.trackIt.sla_service.services;

import com.trackIt.sla_service.client.IndependentServiceClient;
import com.trackIt.sla_service.dto.*;
import com.trackIt.sla_service.exception.AlreadyExistsException;
import com.trackIt.sla_service.exception.ExternalServiceException;
import com.trackIt.sla_service.exception.ServiceException;
import com.trackIt.sla_service.mapper.SlaRulesMapper;
import com.trackIt.sla_service.model.SlaRules;
import com.trackIt.sla_service.repository.SlaRulesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlaRulesServices {

    private final SlaRulesRepository slaRulesRepository;
    private final IndependentServiceClient independentServiceClient;

    @CacheEvict(
            value = {
                    "rules",
                    "ruleById",
                    "ruleByServiceId",
                    "ruleByPriorityId"
            },
            allEntries = true
    )
    @Transactional
    public SlaRulesResponse<Long> addRule(SlaRulesRequest request) {

        log.info("Attempting to add SLA rule for Service ID: {}", request.getServiceId());

        ServicesResponse service =
                independentServiceClient.validateService(request.getServiceId());
        if (service == null) {
            throw new ExternalServiceException("Invalid Service ID: " + request.getServiceId());
        }

        PriorityResponse priority =
                independentServiceClient.validatePriority(request.getPriorityId());
        if (priority == null) {
            throw new ExternalServiceException("Invalid Priority ID: " + request.getPriorityId());
        }

        SlaRules existing =
                slaRulesRepository.findByServiceIdAndPriorityIdAndIsActiveTrue(
                        request.getServiceId(),
                        request.getPriorityId()
                );

        if (existing != null) {
            throw new AlreadyExistsException(
                    "SLA Rule",
                    existing.getSlaId().toString()
            );
        }

        SlaRules deleted =
                slaRulesRepository.findByServiceIdAndPriorityIdAndIsActiveFalse(
                        request.getServiceId(),
                        request.getPriorityId()
                );

        if (deleted != null) {
            log.info("Reactivating SLA rule ID: {}", deleted.getSlaId());
            deleted.activate();
            return SlaRulesMapper.toResponseWithPriorityId(
                    slaRulesRepository.save(deleted)
            );
        }

        try {
            SlaRules slaRules = SlaRulesMapper.toSlaRules(request);
            SlaRules saved = slaRulesRepository.save(slaRules);
            log.info("SLA Rule created successfully");
            return SlaRulesMapper.toResponseWithPriorityId(saved);
        } catch (Exception e) {
            log.error("Failed to save SLA rule", e);
            throw new ServiceException("Failed to save SLA rule", e);
        }
    }


    @Cacheable(value = "rules")
    @Transactional(readOnly = true)
    public List<SlaRulesResponse<String>> getAll() {

        log.info("Attempting to get all SLA rules");

        try {
            // 1️⃣ Fetch all SLA rules
            List<SlaRules> rulesList = slaRulesRepository.findAll();

            if (rulesList.isEmpty()) {
                return List.of();
            }

            // 2️⃣ Fetch unique priority IDs with null-safe handling
            Map<Long, String> priorityMap =
                    rulesList.stream()
                            .map(SlaRules::getPriorityId)
                            .distinct()
                            .collect(Collectors.toMap(
                                    priorityId -> priorityId,
                                    priorityId -> {
                                        try {
                                            PriorityResponse response =
                                                    independentServiceClient.validatePriority(priorityId);

                                            if (response != null && response.getPriorityLevel() != null) {
                                                return response.getPriorityLevel();
                                            }
                                            return "UNKNOWN";
                                        } catch (Exception e) {
                                            log.warn("Failed to fetch priority for ID: {}", priorityId, e);
                                            return "UNKNOWN";
                                        }
                                    }
                            ));

            // 3️⃣ Convert to response DTO
            return SlaRulesMapper.toResponseList(rulesList, priorityMap);

        } catch (Exception e) {
            log.error("Failed to fetch SLA rules", e);
            throw new ServiceException("Failed to fetch SLA rules", e);
        }
    }

    @Cacheable(value = "ruleByServiceId", key = "#serviceId")
    @Transactional(readOnly = true)
    public List<SlaRulesResponse<String>> getByServiceId(Long serviceId) {

        log.info("Fetching SLA rules for Service ID: {}", serviceId);

        try {
            List<SlaRules> rules =
                    slaRulesRepository.findByServiceIdAndIsActiveTrue(serviceId);

            if (rules.isEmpty()) {
                return List.of();
            }

            // Build priority map with null-safe handling
            Map<Long, String> priorityMap =
                    rules.stream()
                            .map(SlaRules::getPriorityId)
                            .distinct()
                            .collect(Collectors.toMap(
                                    priorityId -> priorityId,
                                    priorityId -> {
                                        try {
                                            PriorityResponse response =
                                                    independentServiceClient.validatePriority(priorityId);

                                            if (response != null && response.getPriorityLevel() != null) {
                                                return response.getPriorityLevel();
                                            }
                                            return "UNKNOWN";
                                        } catch (Exception e) {
                                            log.warn("Failed to fetch priority for ID: {}", priorityId, e);
                                            return "UNKNOWN";
                                        }
                                    }
                            ));

            return SlaRulesMapper.toResponseList(rules, priorityMap);

        } catch (Exception e) {
            log.error("Failed to fetch SLA rules for Service ID: {}", serviceId, e);
            throw new ServiceException("Failed to fetch SLA rules by service ID", e);
        }
    }

    @Cacheable(value = "ruleById", key = "#slaRuleId")
    @Transactional(readOnly = true)
    public SlaRulesResponse<String> getBySlaRuleId(Long slaRuleId) {

        log.info("Fetching SLA rule by ID: {}", slaRuleId);

        try {
            SlaRules rule = slaRulesRepository.findById(slaRuleId)
                    .orElseThrow(() ->
                            new ServiceException("SLA rule not found with ID: " + slaRuleId)
                    );

            String priorityName = "UNKNOWN";

            try {
                PriorityResponse priority =
                        independentServiceClient.validatePriority(rule.getPriorityId());

                if (priority != null && priority.getPriorityLevel() != null) {
                    priorityName = priority.getPriorityLevel();
                }
            } catch (Exception e) {
                log.warn("Failed to fetch priority for ID: {}", rule.getPriorityId(), e);
            }

            return SlaRulesMapper.toResponseWithPriorityName(rule, priorityName);

        } catch (Exception e) {
            log.error("Failed to fetch SLA rule with ID: {}", slaRuleId, e);
            throw new ServiceException("Failed to fetch SLA rule", e);
        }
    }

    @CacheEvict(
            value = {
                    "rules",
                    "ruleById",
                    "ruleByServiceId",
                    "ruleByPriorityId"
            },
            allEntries = true
    )
    @Transactional
    public SlaRulesResponse<Long> updateRule(Long slaRuleId, SlaRulesRequest request) {

        log.info("Attempting to update SLA rule with ID: {}", slaRuleId);

        try {
            SlaRules existing = slaRulesRepository.findById(slaRuleId)
                    .orElseThrow(() ->
                            new ServiceException("SLA rule not found with ID: " + slaRuleId)
                    );

            // Validate service & priority
            independentServiceClient.validateService(request.getServiceId());
            independentServiceClient.validatePriority(request.getPriorityId());

            // Check for duplicate (excluding current rule)
            SlaRules duplicate =
                    slaRulesRepository
                            .findByServiceIdAndPriorityIdAndIsActiveTrue(
                                    request.getServiceId(),
                                    request.getPriorityId()
                            );

            if (duplicate != null && !duplicate.getSlaId().equals(slaRuleId)) {
                throw new AlreadyExistsException(
                        "Sla_rule",
                        duplicate.getSlaId().toString()
                );
            }

            // Update fields
            existing.setServiceId(request.getServiceId());
            existing.setPriorityId(request.getPriorityId());
            existing.setResponse_time_mins(request.getResponse_time_mins());
            existing.setResolution_time_mins(request.getResolution_time_mins());
            existing.setUpdatedAt(java.time.LocalDateTime.now());

            SlaRules updated = slaRulesRepository.save(existing);

            log.info("SLA rule updated successfully");

            return SlaRulesMapper.toResponseWithPriorityId(updated);

        } catch (Exception e) {
            log.error("Failed to update SLA rule with ID: {}", slaRuleId, e);
            throw new ServiceException("Failed to update SLA rule", e);
        }
    }

    @CacheEvict(
            value = {
                    "rules",
                    "ruleById",
                    "ruleByServiceId",
                    "ruleByPriorityId"
            },
            allEntries = true
    )
    @Transactional
    public void deleteRule(Long slaRuleId) {

        log.info("Attempting to delete SLA rule with ID: {}", slaRuleId);

        try {
            SlaRules rule = slaRulesRepository.findById(slaRuleId)
                    .orElseThrow(() ->
                            new ServiceException("SLA rule not found with ID: " + slaRuleId)
                    );

            if (!rule.isActive()) {
                log.info("SLA rule already inactive: {}", slaRuleId);
                return;
            }

            rule.deactivate();   // sets isActive = false
            rule.setUpdatedAt(java.time.LocalDateTime.now());

            slaRulesRepository.save(rule);

            log.info("SLA rule soft-deleted successfully");

        } catch (Exception e) {
            log.error("Failed to delete SLA rule with ID: {}", slaRuleId, e);
            throw new ServiceException("Failed to delete SLA rule", e);
        }
    }

    @Cacheable(value = "ruleByPriorityId", key = "#priorityId")
    @Transactional(readOnly = true)
    public List<SlaRulesPriorityResponse> getByPriority(Long priorityId) {

        log.info("Attempting to list services with priority ID: {}", priorityId);

        try {

            List<SlaRules> list = slaRulesRepository
                    .findByPriorityIdAndIsActiveTrue(priorityId);

            return SlaRulesMapper.toSlaPriorityResponseList(list);

        } catch (Exception e) {
            log.error("Failed to fetch list with priority ID: {}", priorityId, e);
            throw new ServiceException(String.format(
                    "Failed to fetch list with priority ID: %d", priorityId),
                    e
            );
        }
    }

}
