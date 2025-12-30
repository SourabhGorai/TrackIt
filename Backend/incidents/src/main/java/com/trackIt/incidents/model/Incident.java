package com.trackIt.incidents.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "incidents")
@Entity
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long incidentId;

    @NotNull(message = "Title required")
    private String title;

    @NotNull(message = "Description is Required")
    private String description;

    @NotNull(message = "Service Id required")
    private Long serviceId;

    @NotNull(message = "Priority Id required")
    private Long priorityId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @NotNull(message = "Reporter Id required")
    private Long reportedBy;                     // will directly fetch from the jwt

    private Long assignedManagerId;              // PROVIDER_MANAGER who accepted the incident

    private Long assignedTo;                     // PROVIDER_MANAGER will assign it to someone later

    @NotNull(message = "Report time required")
    private LocalDateTime reportedAt;

    private LocalDateTime resolvedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
