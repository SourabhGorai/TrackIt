package com.trackIt.user_service.service;

import com.trackIt.user_service.exception.CompanyNotFoundException;
import com.trackIt.user_service.exception.RoleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.independent.url}")
    private String independentServiceUrl;

    /**
     * Verify if role exists
     */
    public void verifyRoleExists(Long roleId) {
        try {
            String url = independentServiceUrl + "/api/roles/validate/" + roleId;
            restTemplate.getForObject(url, Object.class);
            log.info("Role verified: {}", roleId);
        } catch (Exception e) {
            log.error("Role not found: {}", roleId);
            throw new RoleNotFoundException("Role ID " + roleId + " does not exist");
        }
    }

    /**
     * Verify if company exists
     */
    public void verifyCompanyExists(Long companyId) {
        try {
            String url = independentServiceUrl + "/api/companies/validate/" + companyId;
            restTemplate.getForObject(url, Object.class);
            log.info("Company verified: {}", companyId);
        } catch (Exception e) {
            log.error("Company not found: {}", companyId);
            throw new CompanyNotFoundException("Company ID " + companyId + " does not exist");
        }
    }
}