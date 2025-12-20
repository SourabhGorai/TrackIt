package com.trackIt.independent_services.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "companies")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Companies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long companyId;

    @Column(nullable = false, unique = true)
    private String companyName;

    @Column(nullable = false)
    private String companyType;

    @Column(nullable = false)
    private boolean isDeleted;

    public void softDelete() {
        this.isDeleted = true;
    }

    public void restore() {
        this.isDeleted = false;
    }

}
