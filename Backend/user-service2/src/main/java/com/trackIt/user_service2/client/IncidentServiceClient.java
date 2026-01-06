package com.trackIt.user_service2.client;

import com.trackIt.user_service2.dto.ApiResponse;
import com.trackIt.user_service2.dto.response.RoleResponse;
import com.trackIt.user_service2.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class IncidentServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${app.incident-service.url}")
    private String incidentServiceUrl;

    public List<Long> getBusySupportEngineer(List<Long> ids) {
        try {
            log.info("Fetching all busy SUPPORT_ENGINEERS: {}", ids);

            ApiResponse<List<Long>> response = webClientBuilder.build()
                    .post()
                    .uri(incidentServiceUrl + "/supportEngineer/isAvailable")
                    .bodyValue(ids)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Long>>>() {})
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (response != null && response.getSuccess() && response.getData() != null) {
                return response.getData();
            }

            log.warn("Invalid or empty response for user IDs: {}", ids);
            return List.of();

        } catch (Exception e) {
            log.error("Failed to fetch busy support engineers among IDs: {}", ids, e);
            throw new ExternalServiceException(
                    "Unable to fetch busy users. Please try again later",
                    e
            );
        }
    }

}
