package com.trackIt.incidents.client;


import com.trackIt.incidents.dto.*;
import com.trackIt.incidents.dto.response.CompanyResponse;
import com.trackIt.incidents.dto.response.PriorityResponse;
import com.trackIt.incidents.dto.response.RoleResponse;
import com.trackIt.incidents.dto.response.ServicesResponse;
import com.trackIt.incidents.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

            ApiResponse<RoleResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/roles/validate/{roleId}", roleId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<RoleResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for role ID: {}", roleId);
            return null;

        } catch (Exception e) {
            log.error("Failed to validate role with ID: {}", roleId, e);
            throw new ExternalServiceException("Unable to validate role. Please try again later", e);
        }
    }

    public CompanyResponse validateCompany(Long companyId) {
        try {
            log.info("Validating company with ID: {}", companyId);

            ApiResponse<CompanyResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/companies/validate/{companyId}", companyId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CompanyResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for company ID: {}", companyId);
            return null;

        } catch (Exception e) {
            log.error("Failed to validate company with ID: {}", companyId, e);
            throw new ExternalServiceException("Unable to validate company. Please try again later", e);
        }
    }

    public ServicesResponse validateService(Long serviceId){
        try{
            log.info("Fetching Service data with ID: {}", serviceId);

            ApiResponse<ServicesResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/services/{serviceId}", serviceId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ServicesResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for service ID: {}", serviceId);
            return null;

        } catch (Exception e){
            log.error("Failed to fetch services data with ID: {}", serviceId, e);
            throw new ExternalServiceException(
                    "Unable to fetch service data. Please try again later", e);
        }
    }

    public PriorityResponse validatePriority(Long priorityId){
        try{
            log.info("Fetching priority data with ID: {}", priorityId);

            ApiResponse<PriorityResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/priority/{priorityId}", priorityId)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error("HTTP error {} when fetching priority ID: {}",
                                        clientResponse.statusCode(), priorityId);
                                return Mono.empty();
                            }
                    )
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<PriorityResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                log.debug("Successfully fetched priority: {}", response.getData());
                return response.getData();
            }

            log.warn("Invalid or empty response for priority ID: {}", priorityId);
            return null;

        } catch (Exception e){
            log.error("Failed to fetch priority data with ID: {}", priorityId, e);
            throw new ExternalServiceException(
                    "Unable to fetch priority data. Please try again later", e);
        }
    }
}