package com.trackIt.sla_service.client;

import com.trackIt.sla_service.dto.*;
import com.trackIt.sla_service.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndependentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.independent-service.url}")
    private String independentServiceUrl;

    public RoleResponse validateRole(Long roleId) {
        try {
            log.info("Validating role with ID: {}", roleId);

            return webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/roles/validate/{roleId}", roleId)
                    .retrieve()
                    .bodyToMono(RoleResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

        } catch (Exception e) {
            log.error("Failed to validate role with ID: {}", roleId, e);
            throw new ExternalServiceException("Unable to validate role. Please try again later", e);
        }
    }

    public CompanyResponse validateCompany(Long companyId) {
        try {
            log.info("Validating company with ID: {}", companyId);

            return webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/companies/validate/{companyId}", companyId)
                    .retrieve()
                    .bodyToMono(CompanyResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

        } catch (Exception e) {
            log.error("Failed to validate company with ID: {}", companyId, e);
            throw new ExternalServiceException("Unable to validate company. Please try again later", e);
        }
    }

    public ServicesResponse validateService(Long serviceId){
        try{
            log.info("Fetching Service data with ID: {}, from sla-service", serviceId);

            return webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/services/{serviceId}", serviceId)
                    .retrieve()
                    .bodyToMono(ServicesResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e){
            log.error("Failed to fetch services data with Id: {}", serviceId);
            throw new ExternalServiceException(
                    "Unable to fetch service data. Please try again later", e);
        }
    }

    public PriorityResponse validatePriority(Long priorityId){
        try{
            log.info("Fetching priority data with ID: {}, from sla-service", priorityId);

            return webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/priorities/{priorityId}", priorityId)
                    .retrieve()
                    .bodyToMono(PriorityResponse.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
        } catch (Exception e){
            log.error("Failed to fetch priority data with Id: {}", priorityId);
            throw new ExternalServiceException(
                    "Unable to fetch priority data. Please try again later", e);
        }
    }
}