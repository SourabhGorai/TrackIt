package com.trackIt.independent_services.repository;

import com.trackIt.independent_services.model.Companies;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Companies, Long> {

    boolean existsByCompanyName(@NotNull(message = "Company name is required") String companyName);

    boolean existsByCompanyNameAndIsDeletedFalse(String sanitizedCompanyName);

    boolean existsByCompanyNameAndIsDeletedTrue(String sanitizedCompanyName);

    Optional<Companies> findByCompanyNameAndIsDeletedTrue(String sanitizedCompanyName);

    Optional<Companies> findByCompanyNameAndIsDeletedFalse(String sanitizedName);

    List<Companies> findByIsDeletedFalse();

    List<Companies> findByIsDeletedTrue();

    Optional<Companies> findByCompanyName(String sanitizedName);

    List<Companies> findByCompanyType(String companyType);

    // Find active company by ID
    Optional<Companies> findByCompanyIdAndIsDeletedFalse(Long companyId);

    // Check if company exists and is active
    boolean existsByCompanyIdAndIsDeletedFalse(Long companyId);

    boolean existsByCompanyIdAndCompanyTypeAndIsDeletedFalse(
            Long companyId,
            String companyType
    );
}
