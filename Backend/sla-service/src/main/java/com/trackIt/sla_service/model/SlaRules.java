// SlaRules.java - Entity Model
package com.trackIt.sla_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sla_rules")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlaRules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slaId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Priority ID is required")
    private Long priorityId;

    @NotNull(message = "Response time is required")
    @Positive(message = "Response time must be positive")
    private Integer response_time_mins;

    @NotNull(message = "Resolution time is required")
    @Positive(message = "Resolution time must be positive")
    private Integer resolution_time_mins;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @NotNull(message = "Active status required")
    @Builder.Default
    private boolean isActive = true;

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}