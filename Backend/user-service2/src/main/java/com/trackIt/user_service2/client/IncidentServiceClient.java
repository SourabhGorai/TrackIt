package com.trackIt.user_service2.client;

import com.trackIt.user_service2.dto.ApiResponse;
import com.trackIt.user_service2.dto.response.RoleResponse;
import com.trackIt.user_service2.exception.ExternalServiceException;
import jakarta.servlet.http.HttpServletRequest;
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
    private final HttpServletRequest request;

    @Value("${app.incident-service.url}")
    private String incidentServiceUrl;

    private String getAuthHeader() {
        return request.getHeader("Authorization");
    }

    public List<Long> getBusySupportEngineer(List<Long> ids) {
        try {
            log.info("Fetching all busy SUPPORT_ENGINEERS: {}", ids);

            ApiResponse<List<Long>> response = webClientBuilder.build()
                    .post()
                    .uri(incidentServiceUrl + "/incidents/supportEngineer/isAvailable")
                    .bodyValue(ids)
                    .header("Authorization", getAuthHeader())
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
