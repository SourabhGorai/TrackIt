package com.trackIt.incident_service.client;

import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.response.PriorityResponse;
import com.trackIt.incident_service.dto.response.ServicesResponse;
import com.trackIt.incident_service.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Service client for scheduled tasks that don't have HTTP request context.
 * Uses internal service authentication headers compatible with API Gateway.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTaskServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.independent-service.url}")
    private String independentServiceUrl;
    
    @Value("${app.internal-token:notification-service-secret}")
    private String internalToken;

    /**
     * Add internal service authentication headers
     */
    private WebClient.RequestHeadersSpec<?> addInternalHeaders(WebClient.RequestHeadersSpec<?> spec) {
        return spec
                .header("X-Internal-Service", "INCIDENT-SERVICE")
                .header("X-Internal-Token", internalToken);
    }

    public ServicesResponse validateService(Long serviceId) {
        try {
            log.info("Fetching Service data with ID: {} (scheduled task)", serviceId);

            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/services/{serviceId}", serviceId);

            ApiResponse<ServicesResponse> response = addInternalHeaders(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ServicesResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for service ID: {}", serviceId);
            return null;

        } catch (Exception e) {
            log.error("Failed to fetch services data with ID: {}", serviceId, e);
            throw new ExternalServiceException(
                    "Unable to fetch service data. Please try again later", e);
        }
    }

    public PriorityResponse validatePriority(Long priorityId) {
        try {
            log.info("Fetching priority data with ID: {} (scheduled task)", priorityId);

            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get()
                    .uri(independentServiceUrl + "/priority/{priorityId}", priorityId);

            ApiResponse<PriorityResponse> response = addInternalHeaders(request)
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

        } catch (Exception e) {
            log.error("Failed to fetch priority data with ID: {}", priorityId, e);
            throw new ExternalServiceException(
                    "Unable to fetch priority data. Please try again later", e);
        }
    }
}