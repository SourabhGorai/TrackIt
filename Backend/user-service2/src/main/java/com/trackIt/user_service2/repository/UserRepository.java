package com.trackIt.user_service2.repository;
import com.trackIt.user_service2.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

    Optional<Users> findByEmail(String email);

    Optional<Users> findByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeId(String employeeId);

    List<Users> findByCompanyId(Long companyId);

    List<Users> findByCompanyIdAndIsDeletedFalseAndIsAccountLockedFalse(Long companyId);

}
