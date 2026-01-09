package com.trackIt.incident_service.repository;

import com.trackIt.incident_service.model.IncidentSla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentSlaRepository extends JpaRepository<IncidentSla, Long> {

    Optional<IncidentSla> findByIncident_IncidentIdAndIsActiveTrue(Long incidentId);

    // Find incidents approaching response deadline (within warning window)
    @Query("""
            SELECT isla FROM IncidentSla isla
            WHERE isla.isActive = true
            AND isla.respondedAt IS NULL
            AND isla.responseDeadline > :now
            AND isla.responseDeadline <= :warningThreshold
            AND isla.responseWarningPublished = false
            """)
    List<IncidentSla> findApproachingResponseDeadline(
            @Param("now") LocalDateTime now,
            @Param("warningThreshold") LocalDateTime warningThreshold
    );

    // Find incidents approaching resolution deadline (within warning window)
    @Query("""
            SELECT isla FROM IncidentSla isla
            WHERE isla.isActive = true
            AND isla.resolvedAt IS NULL
            AND isla.resolutionDeadline > :now
            AND isla.resolutionDeadline <= :warningThreshold
            AND isla.resolutionWarningPublished = false
            """)
    List<IncidentSla> findApproachingResolutionDeadline(
            @Param("now") LocalDateTime now,
            @Param("warningThreshold") LocalDateTime warningThreshold
    );

    // Find response breaches
    @Query("""
            SELECT isla FROM IncidentSla isla
            WHERE isla.isActive = true
            AND isla.respondedAt IS NULL
            AND isla.responseDeadline < :now
            AND isla.responseBreached = false
            """)
    List<IncidentSla> findResponseBreaches(@Param("now") LocalDateTime now);

    // Find resolution breaches
    @Query("""
            SELECT isla FROM IncidentSla isla
            WHERE isla.isActive = true
            AND isla.resolvedAt IS NULL
            AND isla.resolutionDeadline < :now
            AND isla.resolutionBreached = false
            """)
    List<IncidentSla> findResolutionBreaches(@Param("now") LocalDateTime now);

    List<IncidentSla> findAllByIsActive(boolean b);
}