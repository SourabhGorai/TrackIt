package com.trackIt.independent_services.repository;

import com.trackIt.independent_services.dto.RolesResponse;
import com.trackIt.independent_services.model.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Roles, Long> {
    boolean existsByRole(String role);
    void deleteByRole(String role);

//    @Query("SELECT r.roleId FROM Roles r WHERE r.role = :name")
//    Optional<Long> findRoleIdByName(@Param("name") String name);

    Optional<Roles> findByRole(String name);

}
