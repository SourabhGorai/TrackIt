package com.trackIt.user_service2.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "provider_manager")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProviderManagers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private Users user;

    private LocalTime shiftStart;

    private LocalTime shiftEnd;

    @NotNull(message = "On-call status is required")
    @Column(nullable = false)
    private Boolean onCall;

    public void activateOnCall() {
        this.onCall = true;
    }

    public void deactivateOnCall() {
        this.onCall = false;
    }
}
