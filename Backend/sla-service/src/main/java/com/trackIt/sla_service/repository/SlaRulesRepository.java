package com.trackIt.sla_service.repository;

import com.trackIt.sla_service.model.SlaRules;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlaRulesRepository extends JpaRepository<SlaRules, Long> {

    SlaRules findByServiceIdAndPriorityIdAndIsActiveTrue(Long serviceId, Long priorityId);

    SlaRules findByServiceIdAndPriorityIdAndIsActiveFalse(Long serviceId, Long priorityId);

    List<SlaRules> findByServiceIdAndIsActiveTrue(Long serviceId);

    List<SlaRules> findByPriorityIdAndIsActiveTrue(Long priorityId);
}
