package com.trackIt.independent_services.repository;

import com.trackIt.independent_services.model.Services;
import com.trackIt.independent_services.model.Companies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicesRepository extends JpaRepository<Services, Long> {

    // Find all services for a client company
    List<Services> findByClientCompany(Companies clientCompany);

    // Find all services for a provider company
    List<Services> findByProviderCompany(Companies providerCompany);

    // Find services by client company ID
    List<Services> findByClientCompany_CompanyId(Long clientCompanyId);

    // Find services by provider company ID
    List<Services> findByProviderCompany_CompanyId(Long providerCompanyId);

    // Check if a service exists for a client-provider pair
    boolean existsByClientCompanyAndProviderCompany(
            Companies clientCompany,
            Companies providerCompany
    );

    // Find services by name
    List<Services> findByServiceName(String serviceName);

    boolean existsByServiceNameAndClientCompany_CompanyIdAndProviderCompany_CompanyId(
            String serviceName,
            Long clientCompanyId,
            Long providerCompanyId
    );
}
