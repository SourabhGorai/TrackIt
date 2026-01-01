package com.trackIt.incidents.repository;

import com.trackIt.incidents.model.Incident;
import com.trackIt.incidents.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findFirstByTitleAndServiceIdAndReportedByAndStatusAndCreatedAtAfter(
            String title,
            Long serviceId,
            Long reportedBy,
            Status status,
            LocalDateTime afterTime
    );

    List<Incident> findByServiceIdIn(List<Long> serviceIds);

}
