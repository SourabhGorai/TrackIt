package com.trackIt.sla_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    private int response_time_mins;

    @NotNull(message = "Resolution time is required")
    private int resolution_time_mins;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @NotNull(message = "Active status required")
    private boolean isActive;

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

}
