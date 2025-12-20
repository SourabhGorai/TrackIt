package com.trackIt.user_service.repository;

import com.trackIt.user_service.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmployeeId(String employeeId);

    Optional<Users> findByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);
}