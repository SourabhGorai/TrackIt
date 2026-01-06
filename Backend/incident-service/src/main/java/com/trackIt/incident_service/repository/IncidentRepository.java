package com.trackIt.incident_service.repository;

import com.trackIt.incident_service.model.Incident;
import com.trackIt.incident_service.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
                SELECT DISTINCT i.assignedTo
                FROM Incident i
                WHERE i.assignedTo IN :ids
            """)
    List<Long> findAssignedToIds(@Param("ids") List<Long> ids);


    List<Incident> findAllByAssignedManagerId(Long userId);

    List<Incident> findAllByAssignedTo(Long userId);

    List<Incident> findAllByReportedBy(Long userId);
}
