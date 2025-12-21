package com.trackIt.independent_services.repository;

import com.trackIt.independent_services.model.Priorities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriorityRepository extends JpaRepository<Priorities, Long> {
    boolean existsByPriorityLevel(String priorityLevel);
}
