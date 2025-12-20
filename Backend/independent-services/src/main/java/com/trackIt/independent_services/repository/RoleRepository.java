package com.trackIt.independent_services.repository;

import com.trackIt.independent_services.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Roles, Long> {
    boolean existsByRole(String role);
    void deleteByRole(String role);
}
