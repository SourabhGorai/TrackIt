package com.trackIt.incident_service.client;

import com.trackIt.incident_service.dto.ApiResponse;
import com.trackIt.incident_service.dto.response.PromiseResponse;
import com.trackIt.incident_service.exception.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlaServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.sla-service.url}")
    private String slaServiceUrl;
    private final HttpServletRequest request;

    public PromiseResponse getPromisedTimes(Long serviceId, Long priorityId) {
        String authHeader = request.getHeader("Authorization");
        try {
            log.info("Validating promised time with service ID: {} ad priority ID: {}",
                    serviceId, priorityId);

            ApiResponse<PromiseResponse> response = webClientBuilder.build()
                    .get()
                    .uri(slaServiceUrl + "/getTimes/{serviceId}/{priorityId}",
                            serviceId, priorityId)
                    .header("Authorization", authHeader)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<PromiseResponse>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response");
            return null;

        } catch (Exception e) {
            log.error("Failed to get promised times with Service ID: {}", serviceId, e);
            throw new ExternalServiceException("Unable to validate role. Please try again later", e);
        }
    }

}
