package com.trackIt.incidents.client;

import com.trackIt.incidents.dto.ApiResponse;
import com.trackIt.incidents.dto.response.PromiseResponse;
import com.trackIt.incidents.dto.response.RoleResponse;
import com.trackIt.incidents.exception.ExternalServiceException;
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

    public PromiseResponse getPromisedTimes(Long serviceId, Long priorityId) {
        try {
            log.info("Validating promised time with service ID: {} ad priority ID: {}",
                    serviceId, priorityId);

            ApiResponse<PromiseResponse> response = webClientBuilder.build()
                    .get()
                    .uri(slaServiceUrl + "/getTimes/{serviceId}/{priorityId}",
                            serviceId, priorityId)
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
