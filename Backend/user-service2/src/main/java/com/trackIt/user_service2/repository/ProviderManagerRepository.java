package com.trackIt.user_service2.repository;

import com.trackIt.user_service2.model.ProviderManagers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderManagerRepository extends JpaRepository<ProviderManagers, Long> {
    Optional<ProviderManagers> findByUser_Id(Long userId);
}
