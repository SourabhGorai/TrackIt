package com.trackIt.user_service2.client;

import com.trackIt.user_service2.dto.CompanyResponse;
import com.trackIt.user_service2.dto.RoleResponse;
import com.trackIt.user_service2.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
}