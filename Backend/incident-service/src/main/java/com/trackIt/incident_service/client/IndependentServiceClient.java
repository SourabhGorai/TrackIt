package com.trackIt.incident_service.client;



import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.response.CompanyResponse;
import com.trackIt.incident_service.dto.response.PriorityResponse;
import com.trackIt.incident_service.dto.response.RoleResponse;
import com.trackIt.incident_service.dto.response.ServicesResponse;
import com.trackIt.incident_service.exception.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class IndependentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.independent-service.url}")
    private String independentServiceUrl;
    private final HttpServletRequest request;

    public RoleResponse validateRole(Long roleId) {

        String authHeader = request.getHeader("Authorization");

        try {
            log.info("Validating role with ID: {}", roleId);

            ApiResponse<RoleResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/roles/validate/{roleId}", roleId)
                    .header("Authorization", authHeader)
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
        String authHeader = request.getHeader("Authorization");
        try {
            log.info("Validating company with ID: {}", companyId);

            ApiResponse<CompanyResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/companies/validate/{companyId}", companyId)
                    .header("Authorization", authHeader)
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
        String authHeader = request.getHeader("Authorization");
        try{
            log.info("Fetching Service data with ID: {}", serviceId);

            ApiResponse<ServicesResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/services/{serviceId}", serviceId)
                    .header("Authorization", authHeader)
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
        String authHeader = request.getHeader("Authorization");
        try{
            log.info("Fetching priority data with ID: {}", priorityId);

            ApiResponse<PriorityResponse> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/priority/{priorityId}", priorityId)
                    .header("Authorization", authHeader)
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

    public List<Long> getServiceIdList(Long compId) {

        String authHeader = request.getHeader("Authorization");
        log.info("Fetching service ID list for company ID: {}", compId);

        try {
            ApiResponse<List<Long>> response = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/services/serviceList/{compId}", compId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                log.error(
                                        "HTTP error {} while fetching service IDs for company ID: {}",
                                        clientResponse.statusCode(), compId
                                );
                                return clientResponse
                                        .bodyToMono(String.class)
                                        .flatMap(body ->
                                                Mono.error(new ExternalServiceException(
                                                        "Independent service error: " + body))
                                        );
                            }
                    )
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && Boolean.TRUE.equals(response.getSuccess())
                    && response.getData() != null) {

                log.info("Fetched {} service IDs for company ID: {}",
                        response.getData().size(), compId);

                return response.getData();
            }

            log.warn("Empty response received while fetching service IDs for company ID: {}", compId);
            return List.of();

        } catch (Exception e) {
            log.error("Failed to fetch service IDs for company ID: {}", compId, e);
            throw new ExternalServiceException(
                    "Unable to fetch service IDs. Please try again later", e
            );
        }
    }
}