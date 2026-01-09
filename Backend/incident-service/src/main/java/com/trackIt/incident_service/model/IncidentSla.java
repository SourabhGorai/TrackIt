package com.trackIt.incident_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_sla")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentSla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentSlaId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    // SLA targets
    @NotNull
    private LocalDateTime responseDeadline;

    @NotNull
    private LocalDateTime resolutionDeadline;

    // Actual timestamps (nullable initially)
    private LocalDateTime respondedAt;
    private LocalDateTime resolvedAt;

    // SLA evaluation
    private boolean responseBreached;
    private boolean resolutionBreached;

    // Warning flags to prevent duplicate notifications
    @Builder.Default
    private boolean responseWarningPublished = false;

    @Builder.Default
    private boolean resolutionWarningPublished = false;

    private boolean isActive;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void activate(){
        this.isActive = true;
    }

    public void deactivate(){
        this.isActive = false;
    }
}

/*

I HAVE USED PARTIAL INDEXING
----------------------------

CREATE INDEX idx_sla_response_warning
ON incident_sla (response_deadline)
WHERE is_active = true
  AND responded_at IS NULL
  AND response_warning_published = false;

CREATE INDEX idx_sla_resolution_warning
ON incident_sla (resolution_deadline)
WHERE is_active = true
  AND resolved_at IS NULL
  AND resolution_warning_published = false;

CREATE INDEX idx_sla_response_breach
ON incident_sla (response_deadline)
WHERE is_active = true
  AND responded_at IS NULL
  AND response_breached = false;

CREATE INDEX idx_sla_resolution_breach
ON incident_sla (resolution_deadline)
WHERE is_active = true
  AND resolved_at IS NULL
  AND resolution_breached = false;

*/